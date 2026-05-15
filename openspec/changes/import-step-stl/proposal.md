## Why

ClojCAD can model parametric 3D shapes and export them to STL/STEP, but users cannot import existing models from these standard interchange formats. Adding import unlocks workflows like editing downloaded CAD files, remixing community models, and round-tripping between ClojCAD and other CAD tools.

## What Changes

- Add OCCT reader bindings (`STEPControl_Reader_1`, `StlAPI_Reader`, `BRepBuilderAPI_MakeSolid` and dependencies) used via the existing opencascade.js WASM dist
- Create a new `ClojCAD.kernel.import` namespace wrapping STEP and STL import functions
- Expose `import-step` and `import-stl` functions in the public kernel API
- Support both ASCII and binary STL files (detect format and handle accordingly)
- Add an "Import" button to the three-cad-viewer toolbar with a file picker
- Convert imported shapes into the scene manager's format for display alongside parametric models
- Handle error cases: unsupported formats, corrupt files, and memory management

## Capabilities

### New Capabilities
- `stl-import`: Import STL files (ASCII and binary) into OCCT `TopoDS_Shape` via `StlAPI_Reader`
- `step-import`: Import STEP files (AP203/AP214) into OCCT `TopoDS_Shape` via `STEPControl_Reader_1`
- `import-ui`: File upload button in the three-cad-viewer toolbar to trigger STEP or STL import of a selected file

### Modified Capabilities
- `cad-geometry-kernel`: Extend with import-related OCCT lifecycle management and new reader class initialization
- `interactive-viewer`: Add support for imported shapes in the scene manager (tracking imported shapes alongside parametric model shapes)

## Impact

- **New source file**: `src/ClojCAD/kernel/import.cljs` — STL and STEP import wrappers
- **New source file**: `src/ClojCAD/viewport/import_ui.cljs` — toolbar file-upload button component
- **Modified source**: `src/ClojCAD/kernel/api.cljs` — add `import-stl`, `import-step` to public API
- **Modified source**: `src/ClojCAD/viewport/viewer.cljs` — mount import button after viewer init
- **Modified source**: `src/ClojCAD/kernel/init.cljs` — ensure new OCCT reader classes initialize correctly (they are likely already in the dist)
- **Modified source**: `src/ClojCAD/scene/manager.cljs` — handle imported shapes in the scene model (add imported shapes to registry for display)
- **No new npm dependencies**: STL/STEP import uses OCCT's built-in readers already present in the existing WASM dist
- **No opencascade.js fork rebuild needed**: All reader classes (`STEPControl_Reader_1`, `StlAPI_Reader`, `BRepBuilderAPI_MakeSolid`, `TopoDS_Cast`) are confirmed present in the existing `cascadestudio.d.ts` / WASM dist
