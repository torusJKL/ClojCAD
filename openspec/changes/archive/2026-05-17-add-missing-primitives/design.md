## Context

The kernel (`src/ClojCAD/kernel/primitives.cljs`) wraps four OCCT `BRepPrimAPI` constructors — sphere, box, cylinder, cone — each returning a `TopoDS_Shape`. All follow the same pattern: `js/Reflect.construct` on the OCCT constructor, `.Shape()` extraction, `.delete()` on the builder, `lifecycle/track`.

The zalo opencascade.js fork bundles OCCT classes needed for 2D primitives (edges, wires, faces via `BRepBuilderAPI`). None are wired into the ClojureScript kernel yet.

Key constraints:
- All geometry creation is synchronous (runs on the UI thread in the params watcher)
- Every OCCT heap allocation must be tracked or deleted
- The `defmodel` macro body can be any expression returning a shape
- Tessellation in `kernel/mesh.cljs` handles `TopoDS_Face`, `TopoDS_Wire`, and `TopoDS_Solid` — any shape type works
- Config-style `& {:keys [...]}` is the established pattern in the kernel for optional parameters

## Goals / Non-Goals

**Goals:**
- Add `centered?` option to `make-box` and `make-cylinder` (keyword args)
- Add `make-circle` returning a `TopoDS_Wire` or `TopoDS_Face` based on `wire?`
- Add `make-polygon` returning a `TopoDS_Wire` or `TopoDS_Face` based on `wire?`
- Export all new primitives from `kernel/api.cljs`
- Follow existing patterns: same ns requires, same construct/delete/track lifecycle
- Add WASM-dependent tests for each new function
- Update `cad-geometry-kernel` spec and add new capability specs
- Update `README.org` DSL reference

**Non-Goals:**
- Extrude/Loft/Pipe operations (these consume circles/polygons as inputs but are separate features)
- Transform shorthand on primitives (e.g., `:position` in the arg map)
- Async or worker-thread geometry computation
- Font file loading (use OCCT built-in font through the WASM binary)
- 2D Boolean operations on wire/face shapes
- Chamfer/Fillet or other local operations
- Shape validity verification beyond OCCT's own null/IsNull checks

## Decisions

### 1. Multi-arity for optional parameters

All functions use multi-arity dispatch. The shorter arity calls the longer arity with a default value:

```clojure
(defn make-box
  ([dx dy dz] (make-box dx dy dz false))
  ([dx dy dz centered?]
    ...))
```

This is idiomatic Clojure, keeps signatures positional, and avoids keyword-map overhead. Same pattern for `make-cylinder`, `make-circle`, and `make-polygon`.

**Alternatives considered:**
- `& {:keys [...]}` — more flexible but slower and inconsistent with the rest of the kernel
- Map-based arg — adds ceremony for callers

### 2. `centered?` implementation: post-creation translation

Centering is done by translating the created shape by half its dimensions in the negative direction:

- **Box**: After `BRepPrimAPI_MakeBox_2(dx, dy, dz)`, translate by `(-dx/2, -dy/2, -dz/2)` using `with-trsf` (the existing private helper in `primitives.cljs`)
- **Cylinder**: After `BRepPrimAPI_MakeCylinder_1(radius, height)`, translate by `(0, 0, -height/2)`

This reuses the existing `with-trsf` / `translate` machinery and avoids needing different OCCT constructors.

**Alternatives considered:**
- Using `BRepPrimAPI_MakeBox_1(pnt, dx, dy, dz)` with `gp_Pnt(-dx/2, -dy/2, -dz/2)` — works but introduces a different constructor pattern
- Using `BRepPrimAPI_MakeCylinder_2` (with axis/pnt args) — same concern
- The translation approach is simpler, consistent, and follows the same pattern users would use manually

### 3. `make-circle`: edge → wire → face pipeline

`make-circle` builds geometry through these OCCT steps:

```
wire=true:   gp_Circ(gp_Ax2(gp_Pnt(0,0,0), gp_Dir(0,0,1)), radius)
             → BRepBuilderAPI_MakeEdge_1(circ) → Edge
             → BRepBuilderAPI_MakeWire_1(edge) → Wire
             → return wire

wire=false:  (same edge → wire pipeline)
             → BRepBuilderAPI_MakeFace_1(wire) → Face
             → return face
```

Both the intermediate edge and the final wire/face must be tracked. Intermediate objects (builder) are `.delete()`'d.

The `gp_Ax2` defines the planar coordinate system (origin + normal). The circle lies in the XY plane with Z normal — matching OCCT conventions and making it suitable for `BRepPrimAPI_MakePrism` extrusion in Z.

**Alternatives considered:**
- `BRepBuilderAPI_MakeEdge_2(gp_Circ, u1, u2)` for arcs — not needed; full circle is the common case

### 4. `make-polygon`: point sequence → wire → face pipeline

`make-polygon` accepts a sequence of `[x y]` or `[x y z]` point vectors:

```
points = [[x1 y1 z1] [x2 y2 z2] ... [xn yn zn]]

→ for each point, create gp_Pnt, add to polygon builder
→ BRepBuilderAPI_MakePolygon_1()
→ .Add(pnt1), .Add(pnt2), ..., .Close()
→ .Wire() → Wire
→ optionally BRepBuilderAPI_MakeFace_1(wire) → Face
```

`BRepBuilderAPI_MakePolygon` is used incrementally — constructed empty, then points added via `.Add(gp_Pnt)`, then `.Close()` to close the polygon. This is the standard OCCT pattern.

### 5. Module organization

The new functions live in `primitives.cljs` alongside existing primitives (they share the same concerns: OCCT construction, lifecycle, shape return).

- `src/ClojCAD/kernel/primitives.cljs` — make-box (extended), make-cylinder (extended), make-circle, make-polygon
- `src/ClojCAD/kernel/api.cljs` — re-exports for all new additions

### 6. Testing strategy

Each new primitive gets a WASM-dependent `deftest` following the existing pattern:

```clojure
(deftest make-circle-wire-returns-shape
  (let [shape (sut/make-circle 5 :wire? true)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))
```

Tests cover:
- Each primitive returns a non-null, non-IsNull shape
- Circle with `wire? true` returns a wire (optionally verify via `TopoDS_Wire` cast)
- Circle with `wire? false` returns a face
- Polygon with 3+ points returns a shape
- Polygon with wire? false returns a face
- Box with centered? is at origin (can verify via bounding box location)
- Cylinder with centered? is at origin

### 7. Backward compatibility

- `(make-box 10 20 30)` and `(make-cylinder 5 20)` continue to work identically (3-arity calls the 4-arity with `false`)
- The new primitives (`make-circle`, `make-polygon`) default to `wire? false` in the shorter arity
- No changes to the `defmodel` macro, tessellation, viewer, or scene manager
- No new runtime dependencies

## Risks / Trade-offs

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| `BRepBuilderAPI_MakePolygon` JS binding may have different overload indices than expected | Low | Test with actual WASM; binary search constructor suffix if needed |
| 2D shapes (wire, face) may not tessellate correctly through `mesh.cljs` (which expects solid topology) | Medium | Review `mesh.cljs` edge/face iteration — it already handles faces via `TopExp_Explorer(TopAbs_FACE)`, which works for standalone faces; wires may need special handling |
| `centered?` creates an extra tracked allocation (the translated shape) | Low | Acceptable — one extra tracked shape per centered primitive is negligible |
| Points in `make-polygon` with no Z coordinate need to default to 0 | Low | Use `gp_Pnt_3(x y 0)` when only 2 coords provided |
| BRepBuilderAPI_MakeFace may reject non-planar polygon wires | Medium | Document that polygons must be planar; for non-planar cases users can use `wire? true` |

## Open Questions

(none)
