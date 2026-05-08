## 1. Dependency Setup

- [x] 1.1 Update `package.json`: replace `"opencascade.js": "^1.0.0"` with `"opencascade.js": "github:zalo/opencascade.js#cascadestudio-v2"`
- [x] 1.2 Update `postinstall` script in `package.json`: add WASM copy step `cp node_modules/opencascade.js/dist/cascadestudio.wasm public/`
- [x] 1.3 Run `npm install` and verify `node_modules/opencascade.js` resolves to the zalo fork
- [x] 1.4 Verify `public/cascadestudio.wasm` is present after install
- [x] 1.5 Verify `(require ["opencascade.js" :default init-oc])` compiles in shadow-cljs

## 2. Kernel Init — Load the Zalo Fork

- [x] 2.1 Rewrite `kernel/init.cljs`: import `opencascade.js` via `:default`, call factory with `locateFile` pointing to `/cascadestudio.wasm`
- [x] 2.2 Keep the same atom interface: `oc-instance`, `loading?`, `error`
- [x] 2.3 Remove the `js/Function("return import(...)")` dynamic import hack
- [x] 2.4 Test: page loads, WASM initializes, `oc-instance` is non-nil

## 3. Kernel Primitives — Real OCCT B-Rep

- [x] 3.1 Rewrite `kernel/primitives.cljs`: `make-sphere` calls `oc.BRepPrimAPI_MakeSphere(radius).Shape()`, returns `TopoDS_Shape`
- [x] 3.2 Rewrite `make-box`: `oc.BRepPrimAPI_MakeBox(dx, dy, dz).Shape()`
- [x] 3.3 Add `make-cylinder`, `make-cone` stubs (or implement if needed for demo)
- [x] 3.4 Return raw OC shape handles (not mesh data — tessellation is separate)

## 4. Kernel Tessellation — B-Rep to Mesh

- [x] 4.1 Rewrite `kernel/mesh.cljs`: implement `tessellate` function
- [x] 4.2 Create `BRepMesh_IncrementalMesh` on the input shape with configurable `maxDeviation`
- [x] 4.3 Iterate faces via `TopExp_Explorer(TopAbs_FACE)`
- [x] 4.4 For each face: extract vertices from `Poly_Triangulation`, concatenate into flat vertex array
- [x] 4.5 Extract normals via `.Normal_1(i)`, handle face orientation (reversed/non-reversed)
- [x] 4.6 Extract triangle indices via `.Triangle(nt)`, correct winding for reversed faces
- [x] 4.7 Concatenate per-face vertex/normal/index arrays into single flat typed arrays
- [x] 4.8 Extract edges via `BRepAdaptor_Curve` + `GCPnts_TangentialDeflection` (or `Poly_PolygonOnTriangulation`)
- [x] 4.9 Return `{:vertices Float64Array :normals Float64Array :indices Uint32Array :edges Float64Array}`
- [x] 4.10 Clean up OC heap objects after tessellation

## 5. Kernel Lifecycle — Memory Management

- [x] 5.1 Update `kernel/lifecycle.cljs` to work with the forked API
- [x] 5.2 Ensure shapes created by primitives are tracked and deletable
- [x] 5.3 Add `tessellate` result memory cleanup — mesh data is GC-managed, OC shape handles are freed

## 6. Mesh Builder — Normals and Edges

- [x] 6.1 Update `viewport/mesh_builder.cljs`: `build-mesh` accepts `{:vertices :normals :indices :edges}`
- [x] 6.2 Add `normal` `BufferAttribute` to the `BufferGeometry`
- [x] 6.3 Add edge geometry: create a separate `BufferGeometry` from the edge point array, render as `LineSegments`
- [x] 6.4 Return `{:face-mesh Mesh :edge-mesh LineSegments}`

## 7. Viewport Renderer — Edge Rendering

- [x] 7.1 Update `viewport/render.cljs`: add edge meshes to the scene group alongside face meshes
- [x] 7.2 Clean up both face and edge meshes on scene clear
- [x] 7.3 Update `dispose-object` to handle edge meshes (geometry + material disposal)

## 8. Integration & Test

- [x] 8.1 Wire the init sequence: WASM init → loading state → ready
- [x] 8.2 Verify `demo.cljs` works: `(defmodel sphere [r] (make-sphere r))` → `(show sphere)` → sphere appears with normals and edges
- [x] 8.3 Verify `(swap! params assoc :r 20)` updates the sphere reactively
- [x] 8.4 Verify memory: repeatedly update params, check that WASM heap doesn't grow unbounded
- [x] 8.5 Verify error handling: invalid params, WASM load failure
