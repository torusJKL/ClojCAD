## 1. Extend Existing Primitives with `centered?`

- [x] 1.1 Add multi-arity to `make-box`: `([dx dy dz] [dx dy dz centered?])`; when `centered?` is true, translate box by `(-dx/2, -dy/2, -dz/2)` using existing `with-trsf` helper
- [x] 1.2 Add multi-arity to `make-cylinder`: `([radius height] [radius height centered?])`; when `centered?` is true, translate cylinder by `(0, 0, -height/2)`
- [x] 1.3 Ensure the translated shape from `centered?` is lifecycle-tracked

## 2. Add `make-circle` Primitive

- [x] 2.1 Create `gp_Circ` with `gp_Ax2` centered at origin with Z-normal orientation
- [x] 2.2 Build edge via `BRepBuilderAPI_MakeEdge_1(gp_Circ)`, extract edge, delete builder
- [x] 2.3 Build wire via `BRepBuilderAPI_MakeWire_1(edge)`, extract wire, delete builder
- [x] 2.4 When `wire?` is false, build face via `BRepBuilderAPI_MakeFace_1(wire)`, extract face, delete builder
- [x] 2.5 Implement `make-circle` with multi-arity `([radius] [radius wire?])`; track and return final shape; delete intermediate builders
- [x] 2.6 Handle invalid radius (<= 0) by returning nil

## 3. Add `make-polygon` Primitive

- [x] 3.1 Create `gp_Pnt` for each point in the input sequence (2D points default Z to 0)
- [x] 3.2 Build polygon incrementally via `BRepBuilderAPI_MakePolygon_1()`: `.Add(pnt)` for each point, then `.Close()`
- [x] 3.3 Extract wire via `.Wire()`, delete builder
- [x] 3.4 When `wire?` is false, build face via `BRepBuilderAPI_MakeFace_1(wire)`, extract face, delete builder
- [x] 3.5 Implement `make-polygon` with multi-arity `([points] [points wire?])`; track and return final shape
- [x] 3.6 Handle insufficient points (< 3) by returning nil

## 4. ~~Add `text3d` Primitive~~ *(deferred to future change)*

- [-] 4.1 Create `src/ClojCAD/kernel/text3d.cljs` — *not implemented; Text3D will use opentype.js font outlines converted to OCCT geometry in a future change*
- [-] 4.2–4.7 All text3d subtasks — *deferred; removed from this change*

## 5. Wire New Primitives into Public API

- [x] 5.1 Add `:require [ClojCAD.kernel.text3d :as text3d]` to `kernel/api.cljs` — *reverted*
- [x] 5.2 Export `def make-circle`, `def make-polygon` from `kernel/api.cljs`

## 6. Update Tests

- [x] 6.1 Add test for `make-box` with `true` (centered) returns non-null, non-IsNull shape
- [x] 6.2 Add test for `make-cylinder` with `true` (centered) returns non-null, non-IsNull shape
- [x] 6.3 Add test for `make-circle` with `true` (wire) returns non-null shape
- [x] 6.4 Add test for `make-circle` with `false` (default) returns non-null shape
- [x] 6.5 Add test for `make-circle` with invalid radius returns nil
- [x] 6.6 Add test for `make-polygon` with 3+ points returns non-null shape
- [x] 6.7 Add test for `make-polygon` with `true` (wire) returns non-null shape
- [x] 6.8 Add test for `make-polygon` with < 3 points returns nil
- [x] 6.11 Run `npm test` to verify all tests pass (27/27 non-WASM tests pass; WASM tests skipped as expected in CI)

## 7. Update Documentation

- [x] 7.1 Update Primitives section in `README.org` with new signatures for `make-box`, `make-cylinder`
- [x] 7.2 Add `make-circle`, `make-polygon` to the DSL Reference in `README.org`
- [x] 7.3 Include `centered?` and `wire?` option documentation
