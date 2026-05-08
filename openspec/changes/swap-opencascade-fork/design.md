## Context

The PoC kernel currently uses Three.js geometry as stand-ins for CAD primitives:
- `make-sphere` returns a `SphereGeometry` → `{:vertices [...] :indices [...]}`
- `make-box` returns a `BoxGeometry` → `{:vertices [...] :indices [...]}`
- `tessellate` is the identity function
- opencascade.js WASM loads but is never called

With the zalo fork, we get real OCCT 8.0.0 RC4 B-Rep with ~120 bound classes selected for CAD modeling (primitives, booleans, fillets, loft, meshing, STEP I/O).

## Goals / Non-Goals

**Goals:**
- Replace Three.js geometry stubs with real OCCT B-Rep primitives
- Implement real tessellation (B-Rep → triangle mesh) with vertex normals
- Implement real edge extraction from OCCT topology
- Keep the ClojureScript `defmodel` macro DSL entirely unchanged
- Keep the reactive scene manager (params atom watcher) synchronous
- Serve the WASM binary as a static asset

**Non-Goals:**
- Boolean operations (future capability, enabled by the fork)
- 2D sketch system
- STEP/IGES import/export (enabled by the fork, not wired yet)
- ClojureScript async/await in the scene manager (not needed until booleans)

## Decisions

1. **npm git dependency over npm package**: The zalo fork is not published to npm. Depend on it via `"opencascade.js": "github:zalo/opencascade.js#cascadestudio-v2"`. The `package.json` in the fork has `"main": "dist/cascadestudio.js"` and `"type": "module"`, so `(require ["opencascade.js" :default init-oc])` resolves correctly through shadow-cljs.

2. **CLJS :require over dynamic import**: The current code uses a `js/Function("return import('/opencascade.js')")` hack to bypass shadow-cljs. With the fork's ES module setup, we use a proper CLJS `:require ["opencascade.js" :default init-oc]`. shadow-cljs bundles the JS logic; the WASM binary is loaded at runtime via the Emscripten loader.

3. **WASM served as static file**: The Emscripten-generated `cascadestudio.js` loader needs `cascadestudio.wasm` accessible via HTTP. shadow-cljs's dev server serves from `public/`. A postinstall script copies `node_modules/opencascade.js/dist/cascadestudio.wasm` → `public/cascadestudio.wasm`. The `locateFile` callback in the init options tells the loader where to find it.

4. **Tessellation follows ShapeToMesh.js pattern**: CascadeStudio's `ShapeToMesh.js` proves the tessellation pipeline works with this fork. We extract:
   - Per-face: vertices via `Poly_Triangration.get().Node(i).Transformed()`, normals via `.Normal_1(i)`, triangles via `.Triangle(nt)`
   - Per-edge: edge points via `BRepAdaptor_Curve` (simpler than `Poly_PolygonOnTriangulation`, sufficient for visual edges)
   - Concatenate all faces into flat arrays for Three.js `BufferGeometry`

5. **Main-thread execution, not Worker**: Unlike cascade-core which runs OCCT in a Web Worker, we run it directly on the main thread. This keeps the reactive params watcher synchronous (no promise plumbing). For single-sphere/box tessellation, the computation is fast enough (<1ms). Future boolean operations may require async handling — CLJS 1.12's async/await is available when needed.

6. **Mesh format contracts**:
   - From primitives: `TopoDS_Shape` (OCCT shape handle)
   - From tessellate: `{:vertices Float64Array :normals Float64Array :indices Uint32Array :edges Float64Array}`
   - Edges are a flat array of xyz points: `[x0,y0,z0, x1,y1,z1, x2,y2,z2, ...]`
   - Rendered as non-indexed `LineSegments` with `LineSegmentsGeometry`

7. **Memory management**: OCCT WASM objects allocated via `new oc.ClassName(...)` must be manually freed via `.delete()`. The lifecycle module tracks all allocations. Shapes are deleted after tessellation extracts the mesh data. The mesh data itself is plain JS arrays/typed arrays (GC-managed).

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| WASM heap memory leaks from OC objects | Track all allocations in lifecycle module; delete after tessellation; monitor heap in devtools |
| zalo fork adds 15-20MB WASM download | Load async with loading indicator; same as current approach; acceptable for PoC |
| `locateFile` path breaks in production | Test with shadow-cljs prod build (`:target :browser` with correct public path) |
| zalo fork API differs from upstream opencascade.js | Fork is a superset of the upstream API for the bound classes we need; cascadestudio.yml documents all ~120 bound classes |
| Edge extraction via `BRepAdaptor_Curve` is slower than `Poly_PolygonOnTriangulation` | Acceptable for PoC; can optimize later if hundreds of edges cause perceptible delay |
