## Context

Browser-based parametric CAD tool built in ClojureScript. The geometry kernel is opencascade.js (OpenCascade compiled to WebAssembly via Emscripten). The 3D viewport uses Three.js. The user writes models in a ClojureScript DSL evaluated via nrebel (inline REPL), with changes reflected instantly in the viewer.

No existing code — this is a greenfield PoC with scope limited to a single sphere.

## Goals / Non-Goals

**Goals:**
- Establish the full eval→mesh→render pipeline end-to-end
- `defmodel` macro with reactive parameter tracking
- Shared params atom drives incremental re-evaluation
- Layered viewer with per-model opacity and visibility toggles
- Reagent-powered layer panel; direct Three.js for the viewport
- Tagging of intermediate geometry (opt-in)

**Non-Goals:**
- 2D sketch system (future capability)
- Boolean operations (future)
- File I/O / STEP export
- Editor extension integration
- Performance optimization beyond what's needed for responsiveness

## Decisions

1. **Shadow-cljs over Figwheel**: Shadow-cljs has first-class nrepl support and straightforward npm dependency management. Its `:npm-module` target works naturally with opencascade.js and Three.js.

2. **Reagent for chrome only, direct Three.js for viewport**: The 3D scene is fully replaced on each model evaluation. Reagent's reconciler adds overhead without benefit for a full-scene swap. Direct calls to Three.js (`scene.clear()`, `scene.add()`) are simpler and give full control over GPU memory. The layer panel and any future UI (param sliders, history) use Reagent since those benefit from incremental DOM updates.

3. **Reactive model pattern**: Each `defmodel` expands to a closure that captures a parameter schema and a computation function. The scene manager maintains a map of `model-name → {fn, last-params, last-mesh, opts}`. When the shared params atom changes, the watcher diffs old vs new keys and invokes only models whose declared parameter keys intersect with changed keys.

4. **Idempotent `show`**: `show` calls the model function and registers/updates a single layer slot keyed by model name. Subsequent `show` calls for the same model update the existing layer rather than adding a duplicate. This keeps the viewer state predictable.

5. **Opt-in `tag` macro**: `(tag :name shape)` is a pass-through that returns shape unchanged but registers an intermediate in the scene hierarchy. Implemented as a dynamic var binding — `defmodel` sets `*scene-context*`, `tag` checks it and registers if present. Zero cost when not used.

6. **opencascade.js WASM initialization**: Async, must complete before any model evaluation. Handled via a promise that the scene manager awaits. The viewer shows a loading state until the kernel is ready.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| opencascade.js WASM file is large (~15-20MB) | Load async, show loading indicator; cache via ServiceWorker for subsequent loads |
| WASM heap memory leaks from OC objects | Wrap OC object creation in `try/finally` with explicit `delete()` calls; monitor heap size in dev tools |
| nrebel connection latency on each eval | Eval in the browser context is instant — nrebel is the transport; if latency becomes an issue, batch evals or use figwheel's file-watch mode as fallback |
| Full scene rebuild on param change may be slow for complex models | Acceptable for PoC; incremental mesh diff could be layered on later |
| Browser GC pauses during mesh updates | Keep mesh data small for PoC (single sphere); revisit for production |
