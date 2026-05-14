## 1. Dependency & Build Setup

- [x] 1.1 Update `package.json`: replace `three@^0.160.0` with `three-cad-viewer@^4.3.9`; remove `react`, `react-dom` (no longer needed with Reagent panel removed)
- [x] 1.2 Run `npm install` and verify resolution of three-cad-viewer and its transitive deps (three@0.184.0, n8ao, postprocessing)
- [x] 1.3 Remove `scripts/patch-three.js` and the postinstall step that runs it
- [x] 1.4 Add `<link rel="stylesheet" href="node_modules/three-cad-viewer/dist/three-cad-viewer.css">` to `public/index.html`
- [x] 1.5 Restructure `public/index.html`: replace `#viewport` + `#ui` with a single `#cad-view` container for the library Display
- [x] 1.6 Verify shadow-cljs compiles successfully with the new dependency; add ESM bridge to `js/external-index.js` if needed

## 2. Viewer Module

- [x] 2.1 Create `src/CADscript/viewport/viewer.cljs` with `init-viewer!` function: creates `Display` + `Viewer` with config options (dark theme, Z-up, tree width 240)
- [x] 2.2 Store `Viewer` and `Display` instances in atoms for access by scene manager
- [x] 2.3 Implement the notification callback: parse tree panel state changes and update scene atom visibility
- [x] 2.4 Remove `viewport/scene.cljs`, `viewport/controls.cljs`, `viewport/render.cljs`, `viewport/mesh_builder.cljs` from the source tree and from `core.cljs` requires

## 3. Shape Adapter

- [x] 3.1 Implement `tessellation→shape` conversion function: rename `:indices` → `:triangles`, convert to JS object with all 9 fields
- [x] 3.2 Implement `build-part` function: wraps shape data with id, name, type/subtype, color, alpha, state, loc for a library Part
- [x] 3.3 Implement `build-shapes-tree` function: assembles a model and its tagged children into the hierarchical `Shapes` structure

## 4. Kernel Metadata Expansion

- [x] 4.1 Modify `extract-faces` in `kernel/mesh.cljs` to compute `:face-types` via `BRepAdaptor_Surface.GetSurfaceType()` for each face
- [x] 4.2 Modify `extract-faces` to record `:triangles-per-face` (triangle count per face)
- [x] 4.3 Modify `extract-faces` to collect `:obj-vertices` (unique vertices per face)
- [x] 4.4 Modify `extract-edges` to compute `:edge-types` via `BRepAdaptor_Curve.GetCurveType()` for each edge
- [x] 4.5 Modify `extract-edges` to record `:segments-per-edge` (segment count per edge)
- [x] 4.6 Update `tessellate` return map to include all new fields

## 5. Scene Manager Adaptation

- [x] 5.1 Add `*viewer` atom reference to scene manager (or pass via dependency injection)
- [x] 5.2 Modify `show` function: after tessellation, call `viewer.addPart("/<model-name>", part)` with shape data and display options
- [x] 5.3 Modify param watcher: on re-evaluation, call `viewer.updatePart("/<model-name>", newPart)` instead of triggering `update-viewport!`
- [x] 5.4 Modify `hide` / `show-model`: call `viewer.removePart` / `viewer.addPart` as appropriate
- [x] 5.5 Modify tagged geometry: add tagged shapes as child parts via `viewer.addPart("/<model-name>/<tag-label>", childPart)`
- [x] 5.6 Implement batching via `{skipBounds: true}` for param changes that update multiple models, followed by a single `viewer.updateBounds()`
- [x] 5.7 Implement notification callback handler: parse change payload and update `scene` atom visibility, tags-visible

## 6. Core Init & Cleanup

- [x] 6.1 Update `core.cljs` `init` function: replace `init-viewport!` + `init-controls!` + `start-loop!` with single `init-viewer!` call
- [x] 6.2 Remove `Reagent` dependency and `react`/`react-dom` requires from core.cljs (no longer importing layer_panel)
- [x] 6.3 Verify demo renders correctly: sphere + box appear in viewer with tree panel entries

## 7. Verification

- [x] 7.1 Start `npx shadow-cljs watch dev` and confirm clean compilation
- [x] 7.2 Open browser and verify: viewport renders, controls work (orbit/pan/zoom), tree panel shows sphere and box
- [x] 7.7 Verify library CSS is applied correctly (no layout issues with the dark theme)
- [x] 7.3 Test param reactivity: `(swap! params assoc :r 20)` at REPL updates sphere in viewer
- [x] 7.4 Test visibility: tree panel toggles hide/show models; scene atom stays in sync
- [x] 7.5 Test tagged geometry: create a model with `tag` and verify hierarchical tree display
- [x] 7.6 Test remove/hide APIs via REPL
- [x] 7.8 Remove `ui/layer_panel.cljs` file
