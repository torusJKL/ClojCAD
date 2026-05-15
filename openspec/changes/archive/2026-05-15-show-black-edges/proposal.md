## Why

The three-cad-viewer has a "Show black edges" toggle button that should display/hide black edge lines on 3D CAD objects. Currently, clicking this button has no visible effect — edges are not appearing on objects rendered from OCCT tessellation, making the feature non-functional.

## What Changes

- Add `blackEdges` option to the viewer initialization to match toggle button state
- Ensure edge data from OCCT tessellation is correctly surfaced through the cad-viewer integration pipeline
- Verify and fix the shape data format so three-cad-viewer renders edge geometry
- Wire the "black edges" toolbar toggle to actually show/hide edges on displayed objects

## Capabilities

### New Capabilities
- `edge-visibility`: Edge rendering and visibility control for OCCT-tessellated shapes in three-cad-viewer

### Modified Capabilities
- `cad-viewer-integration`: Update shape data construction to ensure edge data is passed in the correct format expected by three-cad-viewer's rendering pipeline

## Impact

- `src/ClojCAD/viewport/viewer.cljs` — viewer initialization may need `blackEdges` option
- `src/ClojCAD/viewport/shape_adapter.cljs` — may need format adjustments for edge data
- `src/ClojCAD/scene/manager.cljs` — may need to pass edge visibility state through update/show paths
- `src/ClojCAD/kernel/mesh.cljs` — edge extraction may need verification but likely OK
