## 1. Project Setup

- [x] 1.1 Create shadow-cljs project with `shadow-cljs.edn` and `package.json` (dependencies: opencascade.js, three, reagent, reagent)
- [x] 1.2 Configure `:dev` build target with `:npm-module` and nrepl support
- [x] 1.3 Create `index.html` entry point with Three.js viewport container and Reagent mount point
- [x] 1.4 Create `src/main/core.cljs` with application bootstrap
- [x] 1.5 Verify shadow-cljs compiles, nrepl connects, and page loads

## 2. CAD Geometry Kernel

- [x] 2.1 Create `src/kernel/init.cljs` — async WASM initialization with loading state
- [x] 2.2 Create `src/kernel/primitives.cljs` — `make-sphere`, `make-box` wrappers over opencascade.js interop
- [x] 2.3 Create `src/kernel/mesh.cljs` — tessellation: OC shape → `{:vertices [...] :indices [...]}` map
- [x] 2.4 Create `src/kernel/lifecycle.cljs` — OC object tracking and `destroy` / cleanup
- [x] 2.5 Create `src/kernel/api.cljs` — unified API facade re-exporting all kernel functions

## 3. defmodel Macro & Reactive Model System

- [x] 3.1 Create `src/model/core.cljs` — `reactive-model` constructor: captures param schema + fn, supports caching
- [x] 3.2 Create `src/model/defmodel.clj` — `defmodel` macro: expands to reactive-model, extracts param keys, handles metadata (opacity)
- [x] 3.3 Create `src/model/tag.cljs` — `tag` macro with `*scene-context*` dynamic var for opt-in intermediate registration
- [x] 3.4 Create `src/model/registry.cljs` — model registry: `model-name → {fn, param-keys, last-params, last-mesh, opts}`

## 4. Scene Manager

- [x] 4.1 Create `src/scene/manager.cljs` — scene state: atom holding `{model-name → {:mesh, :opts, :tags}}`
- [x] 4.2 Implement `show` function: idempotent model display with merge over shared params atom
- [x] 4.3 Implement params atom watcher: diff changed keys, identify dirty models, re-evaluate, push new meshes
- [x] 4.4 Implement visibility toggle: hide/show meshes without re-evaluation
- [x] 4.5 Implement opacity support: per-model transparency via Three.js material

## 5. Three.js Viewport

- [x] 5.1 Create `src/viewport/scene.cljs` — Three.js scene, camera, renderer setup
  - [x] Background color `0x222222`, ambient + directional lights
  - [x] GridHelper and AxesHelper for visual reference
- [x] 5.2 Create `src/viewport/controls.cljs` — OrbitControls integration
- [x] 5.3 Create `src/viewport/render.cljs` — `update-viewport!` function: clear scene, add meshes from scene manager
- [x] 5.4 Create `src/viewport/mesh_builder.cljs` — convert `{:vertices [...] :indices [...]}` to Three.js `BufferGeometry` + `Mesh`

## 6. Reagent Layer Panel

- [ ] 6.1 Create `src/ui/layer_panel.cljs` — Reagent component rendering model list with checkboxes
- [ ] 6.2 Implement sub-layer disclosure for tagged intermediates
- [ ] 6.3 Wire layer checkboxes to scene manager visibility toggles
- [ ] 6.4 Style the layer panel (minimal CSS, functional over aesthetic)

## 7. Integration & PoC

- [ ] 7.1 Wire bootstrap sequence: WASM init → show loading → ready → enable REPL commands
- [ ] 7.2 Create `src/demo.cljs` — PoC file: `(def params (atom {:r 10}))`, `(defmodel sphere [r] (make-sphere r))`, `(show sphere)`
- [ ] 7.3 Verify end-to-end flow: load page → sphere appears → `(swap! params assoc :r 20)` at REPL → sphere updates
- [ ] 7.4 Verify optional features: transparency via metadata, tag intermediate, layer toggle

## 8. Polish

- [ ] 8.1 Handle error states gracefully (WASM load failure, invalid params)
- [ ] 8.2 Add OC object cleanup on model re-evaluation to prevent WASM heap growth
- [ ] 8.3 Verify nrebel workflow: eval individual forms, see viewer update without full page reload
