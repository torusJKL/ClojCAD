## Why

ClojCAD can model parametric 3D shapes using OCCT's B-Rep kernel, but has no way to export those shapes to standard interchange formats. Users need STL (for 3D printing, mesh-based workflows) and STEP (for CAD interoperability with other tools). The opencascade.js fork already enables STEP I/O at the C++ level, and STL export can leverage OCCT's built-in `StlAPI_Writer` — neither is wired into ClojureScript.

## What Changes

- Add OCCT binding classes (`StlAPI_Writer`, `STEPControl_Writer`, `STEPControl_Controller`, and their dependencies) to the opencascade.js fork's build configuration
- Rebuild the `cascadestudio.wasm` module with the new bindings
- Create a new `ClojCAD.kernel.export` namespace wrapping STL and STEP export functions
- Expose `export-stl` and `export-step` functions in the public kernel API
- Add an "Export" dropdown button to the three-cad-viewer toolbar with STL and STEP options
- Handle file download in the browser (Blob + download link) since ClojCAD runs in-browser
- Add proper error handling for unsupported shapes, write failures, and memory management

## Capabilities

### New Capabilities
- `stl-export`: Export OCCT B-Rep shapes to STL binary format via `StlAPI_Writer`
- `step-export`: Export OCCT B-Rep shapes to STEP AP203/AP214 format via `STEPControl_Writer`
- `export-ui`: Dropdown button in the three-cad-viewer toolbar to trigger STL or STEP export of the currently displayed shape

### Modified Capabilities
- `cad-geometry-kernel`: Extend with export-related OCCT lifecycle management and new bound class initialization
- *(no other spec-level behavior changes)*

## Impact

- **opencascade.js fork**: Must be rebuilt from source (Docker) with ~10-15 additional OCCT class bindings (StlAPI_Writer, STEPControl_Writer, STEPControl_Controller, STEPControl_Writer, XCAFDoc, TDocStd, etc.)
- **New source file**: `src/ClojCAD/kernel/export.cljs` — STL and STEP export wrappers
- **New source file**: `src/ClojCAD/viewport/export_ui.cljs` — toolbar dropdown button component
- **Modified source**: `src/ClojCAD/kernel/api.cljs` — add `export-stl`, `export-step` to public API
- **Modified source**: `src/ClojCAD/viewport/viewer.cljs` — mount export dropdown after viewer init
- **Modified source**: `src/ClojCAD/kernel/init.cljs` — ensure new OCCT classes initialize correctly
- **New dependency**: Docker (for building opencascade.js fork) — developer-only, not runtime
- **No new npm dependencies**: STL/STEP export uses OCCT's built-in writers, no additional libraries
