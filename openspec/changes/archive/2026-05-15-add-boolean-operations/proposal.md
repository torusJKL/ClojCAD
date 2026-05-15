## Why

The project can create individual OCCT B-Rep primitives (sphere, box, cylinder, cone) but cannot combine them into complex shapes via CSG operations. The zalo opencascade.js fork already bundles `BRepAlgoAPI` classes (Fuse, Common, Cut), making boolean operations available at the OCCT level — but they are not wired into the ClojureScript kernel. Adding them unlocks parametric models built from combined shapes (e.g., a cylinder subtracted from a box for a hole, or intersected volumes).

## What Changes

- **`kernel/booleans.cljs`** (new): Wrap OCCT `BRepAlgoAPI_Fuse`, `BRepAlgoAPI_Common`, and `BRepAlgoAPI_Cut` as ClojureScript functions that accept two `TopoDS_Shape` handles and return a new combined `TopoDS_Shape`
- **`kernel/api.cljs`**: Re-export the new boolean functions (`fuse`, `common`, `cut`) alongside existing primitives
- **`scene/manager.cljs`**: Support multi-shape models that feed two (or more) primitive results into a boolean operation — models can chain primitives and booleans in their body
- **`demo.cljs`**: Add at least one boolean demo (e.g., a box with a cylinder cut out, or intersected sphere/box)
- **Lifecycle**: Boolean result shapes and intermediate shapes must all be tracked for WASM heap cleanup

## Capabilities

### New Capabilities
- `boolean-operations`: OCCT BRepAlgoAPI boolean/CSG operations (fuse/union, common/intersection, cut/difference) operating on TopoDS_Shape handles before tessellation

### Modified Capabilities
- (none — this is purely additive; existing primitive, tessellation, and model capabilities are unchanged)

## Impact

- `src/ClojCAD/kernel/booleans.cljs` — new file wrapping `BRepAlgoAPI_Fuse`, `BRepAlgoAPI_Common`, `BRepAlgoAPI_Cut`
- `src/ClojCAD/kernel/api.cljs` — three new exported symbols
- `src/ClojCAD/demo.cljs` — boolean demo model definitions
- Potentially `src/ClojCAD/scene/manager.cljs` — if boolean computation warrants async handling (but start synchronous)
- No new dependencies — OCCT classes are already available in the zalo fork
- No changes to tessellation, viewer, or model macro system
