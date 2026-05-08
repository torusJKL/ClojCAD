## Why

The current PoC uses Three.js geometry as stubs for sphere and box primitives — it has no real CAD geometry kernel. The `opencascade.js` npm dependency is loaded but never used for actual B-Rep operations. To build a real parametric CAD tool, we need:

1. Real OCCT B-Rep primitives (spheres, boxes, cylinders, etc.)
2. Real tessellation (B-Rep shape → triangle mesh)
3. Real edge extraction for visual edges in the viewport

The zalo fork of opencascade.js (`cascadestudio-v2` branch) is the right foundation: it tracks OCCT 8.0.0 RC4, builds a slim ~120-class WASM module (down from 2000+), and is already used in production by Cascade Studio. CascadeStudio's `ShapeToMesh.js` demonstrates the complete tessellation pipeline including vertex, normal, and edge extraction.

## What Changes

- **Dependency swap**: `opencascade.js` → `github:zalo/opencascade.js#cascadestudio-v2`
- **`kernel/init.cljs`**: Import the zalo fork via CLJS `:require` instead of dynamic `js/Function("import(...)")`
- **`kernel/primitives.cljs`**: Replace Three.js geometry stubs with real OCCT B-Rep primitives (`BRepPrimAPI_MakeSphere`, `BRepPrimAPI_MakeBox`)
- **`kernel/mesh.cljs`**: Replace identity function with real tessellation via `BRepMesh_IncrementalMesh` + `Poly_Triangulation`, producing `{:vertices :normals :indices :edges}`
- **`kernel/lifecycle.cljs`**: Update OC object tracking for the forked API
- **`viewport/mesh_builder.cljs`**: Add support for normals (per-vertex `BufferAttribute`) and edges (`LineSegments`)
- **`viewport/render.cljs`**: Render edge meshes alongside face meshes
- **`scripts/`**: Add WASM binary copy to postinstall
- **`shadow-cljs.edn`**: Update for CLJS 1.12.145+ if needed

## Capabilities

### Modified Capabilities
- `cad-geometry-kernel`: Real OCCT primitives, real tessellation, vertex/normal/edge extraction, lifecycle management for forked WASM

### New Capabilities
- (none — this is a kernel replacement, not a new capability)

### Unchanged Capabilities
- `parametric-model`: defmodel macro, parameter tracking, model registry — all unchanged
- `interactive-viewer`: Scene manager, layer panel, viewport, controls — unchanged except mesh builder adds normals and edges

## Impact

- `package.json`: one dependency changed, one postinstall step added
- `src/CADscript/kernel/`: all four files rewritten
- `src/CADscript/viewport/mesh_builder.cljs`: updated for normals + edges
- `src/CADscript/viewport/render.cljs`: updated to render edges
- `node_modules/opencascade.js/dist/cascadestudio.wasm` copied to `public/` on install
- No changes to the model macro system, scene manager, layer panel, or viewport scene/controls
