## Why

ClojCAD's geometry kernel currently exposes only four 3D primitives (sphere, box, cylinder, cone) with fixed signatures. Users cannot create 2D profiles for extrusion, lofting, or piping, cannot center primitives at the origin, and cannot generate 3D text. Adding these missing primitives makes the kernel competitive for real CAD workflows and unblocks downstream features like extrusion and sweeping.

## What Changes

- **`make-box` signature extended**: `(make-box dx dy dz)` → `(make-box dx dy dz centered?)` — when `centered?` is true, center the box at the origin; 3-arity overload defaults to false
- **`make-cylinder` signature extended**: `(make-cylinder radius height)` → `(make-cylinder radius height centered?)` — when `centered?` is true, center the cylinder at the origin; 2-arity overload defaults to false
- **`make-circle`** (new): Create a 2D circle — `(make-circle radius wire?)` — when `wire?` is true, return a TopoDS_Wire for use in Loft/Pipe; when false (default), return a TopoDS_Face for Extrude; 1-arity overload defaults to false
- **`make-polygon`** (new): Create a 2D polygon from points — `(make-polygon points wire?)` — same wire/face semantics as make-circle; 1-arity overload defaults to false
- **`kernel/api.cljs`**: Export all new primitives
- **`README.org`**: Document new primitives in the DSL reference
- **Tests**: Add WASM-dependent tests for each new primitive

## Capabilities

### New Capabilities
- `circle-primitive`: 2D circle creation via OCCT BRepBuilderAPI_MakeEdge/MakeWire/MakeFace, supporting both wire and face output modes
- `polygon-primitive`: 2D polygon creation from point sequences via OCCT BRepBuilderAPI_MakePolygon, supporting both wire and face output modes
- `centered-primitives`: Optional `centered?` parameter on box and cylinder to center the shape at the origin

### Modified Capabilities
- `cad-geometry-kernel`: Existing spec requires updates — add requirements for `make-circle`, `make-polygon`, and the extended `make-box`/`make-cylinder` signatures with `centered?`

## Impact

- `src/ClojCAD/kernel/primitives.cljs` — modify `make-box` and `make-cylinder` signatures; add `make-circle`, `make-polygon`, `text3d`
- `src/ClojCAD/kernel/api.cljs` — five new exported symbols
- `test/ClojCAD/kernel/primitives_test.cljs` — new tests for each primitive
- `README.org` — DSL reference docs for new primitives
- No new dependencies — all OCCT classes are already available in the zalo fork
- No changes to tessellation, viewer, scene manager, or defmodel macro
