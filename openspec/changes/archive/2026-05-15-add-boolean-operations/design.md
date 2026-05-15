## Context

The kernel currently wraps four OCCT `BRepPrimAPI` constructors (`make-sphere`, `make-box`, `make-cylinder`, `make-cone`) in `kernel/primitives.cljs`. Each returns a `TopoDS_Shape` handle. Models defined via `defmodel` call one primitive and the result is tessellated and displayed.

The zalo opencascade.js fork (`cascadestudio-v2`) binds OCCT `BRepAlgoAPI` classes, which implement the full OCCT boolean operation pipeline on B-Rep shapes. These classes are available on the global OCCT instance but are not yet wrapped.

Key constraints from the codebase:
- The scene manager evaluates models synchronously (no async/await in the params watcher)
- The `defmodel` macro body can be any ClojureScript expression that returns a shape
- Tessellation (`kernel/mesh.cljs`) works on any `TopoDS_Shape` — boolean results are just more shapes
- All OCCT heap allocations are manually tracked/deleted via `kernel/lifecycle.cljs`
- `BRepAlgoAPI` operations can fail (null/invalid result) or be slow on complex shapes

## Goals / Non-Goals

**Goals:**
- Wrap `BRepAlgoAPI_Fuse`, `BRepAlgoAPI_Common`, `BRepAlgoAPI_Cut` as ClojureScript functions
- Follow the same `js/Reflect.construct → .Shape() → .delete()` pattern as primitives
- Track all intermediate allocations (builders, result shapes) in the lifecycle system
- Handle null/invalid results gracefully (return nil, log warning)
- Demonstrate boolean usage in demo models (e.g., box minus cylinder, intersected sphere/box)
- Wire new functions into `kernel/api.cljs` so they're accessible as `kernel/fuse`, `kernel/common`, `kernel/cut`

**Non-Goals:**
- Async boolean evaluation (start synchronous; async only if profiling shows need)
- Shape validity checks before boolean operations
- Preview / intermediate shape caching in the scene manager
- Worker-thread offloading

## Decisions

1. **Variadic API with sequential chaining**: `(fuse a b & more)`, `(common a b & more)`, `(cut a b & more)` — each requires at least two shapes and accepts additional ones. Semantics:
   - `(fuse a b c)` = `(fuse (fuse a b) c)` — left fold, a ∪ b ∪ c
   - `(common a b c)` = `(common (common a b) c)` — left fold, a ∩ b ∩ c
   - `(cut a b c)` = `(cut (cut a b) c)` — left fold, a − b − c
   
   The first two args are mandatory; the `& more` rest args chain sequentially. This mirrors OCCT's binary `BRepAlgoAPI` at the leaf but provides ergonomic variadic entry points.

2. **Binary helper + variadic public API**: A private `-fuse-2`, `-common-2`, `-cut-2` handles the binary OCCT call. The public `fuse`, `common`, `cut` perform a left reduce, short-circuiting on `nil` (if any step fails, return nil for the whole chain).

   ```clojure
   (defn- -fuse-2 [shape-a shape-b]
     (let [ctor (.-BRepAlgoAPI_Fuse (oc))
           builder (js/Reflect.construct ctor #js [shape-a shape-b])
           shape (.Shape builder)]
       (.delete builder)
       (when (and shape (not (.IsNull shape)))
         (lifecycle/track shape)
         shape)))

   (defn fuse [a b & more]
     (let [result (-fuse-2 a b)]
       (if (nil? result)
         nil
         (reduce (fn [acc s] (when acc (-fuse-2 acc s))) result more))))
   ```

3. **Error handling**: If `.Shape()` returns null/undefined, or if `.IsNull()` is true, or if the builder throws, return `nil`. The caller (model body or scene manager) handles nil — the defmodel macro allows nil shapes (they simply produce no mesh).

4. **No builder done check**: OCCT `BRepAlgoAPI` has a `.IsDone()` method. Skip it initially — if the operation fails, `.Shape()` returns null. Add `.IsDone()` check only if null shape alone proves insufficient for error diagnosis.

5. **Lifecycle ownership**: Input shapes are NOT destroyed by the boolean operation — ownership stays with the caller. Only the result shape and the builder intermediate are managed. This lets users pass the same primitive shape into multiple boolean operations without unexpected destruction.

6. **Model integration through composition**: No changes needed to `defmodel` or `scene/manager.cljs` for basic usage. A model can compose primitives:

   ```clojure
   (defmodel with-hole [r w d h]
     (let [box (kernel/make-box w d h)
           cylinder (kernel/make-cylinder r h)]
       (kernel/cut box cylinder)))
   ```

   The scene manager calls `kernel/tessellate` on the result shape just like any other model.

7. **Single file, not inline in primitives**: A dedicated `kernel/booleans.cljs` file keeps concerns separated. Primitives create shapes; booleans combine them.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Boolean operation returns null/invalid shape for certain geometry combinations | Return nil from wrapper; demo models should handle gracefully; add `.IsDone()` check if needed |
| WASM heap fragmentation from repeated boolean operations | Lifecycle tracking already handles cleanup; monitor in devtools |
| Boolean computation blocks UI thread (slow on complex shapes) | Start synchronous; if profiling shows >50ms, add a `kernel/async-booleans.cljs` wrapper using CLJS `promise` / `async` |
| Input shapes are inadvertently deleted while still in use | Decision #5: booleans never delete input shapes — only the builder and result |
| Edge cases: touching shapes, non-manifold results | OCCT BRepAlgoAPI handles these; nil result on failure is acceptable |
