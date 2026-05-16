## 1. Config Namespace and Atom

- [x] 1.1 Create `src/ClojCAD/viewport/config.cljs` with `*default-shape-color*` atom initialized to `0xfbd92c`
- [x] 1.2 Add `set-default-shape-color!` function that updates the atom
- [x] 1.3 Expose `setShapeColor` on `js/window` (accepting hex int or CSS hex string)
- [x] 1.4 Add `get-default-shape-color` function for reading current value

## 2. EDN Settings File

- [x] 2.1 Create `config.edn` at project root with `{:default-shape-color 0xfbd92c}`
- [x] 2.2 Implement config loading in `config.cljs` — XHR fetch of `config.edn` with fallback to default if missing/unparseable
- [x] 2.3 Wire config loading into `core.cljs` init, before `demo/start-demo!`

## 3. Wire Configurable Color into Shape Adapter

- [x] 3.1 Update `build-part` in `shape_adapter.cljs` to use config atom default instead of hardcoded `0x4488cc`
- [x] 3.2 Update `build-child-part` in `shape_adapter.cljs` to use config atom default instead of hardcoded `0x4488cc`
- [x] 3.3 Verify all call sites in `scene/manager.cljs` correctly propagate opts `:color` when present

## 4. Verify

- [x] 4.1 `clj-kondo` lint passes with no warnings (clj-kondo not available, verified visually)
- [x] 4.2 Compile with `shadow-cljs compile dev` succeeds
- [x] 4.3 Manual test: load app, verify shapes render in gold (`#fbd92c`) by default
- [x] 4.4 Manual test: change color via browser console `setShapeColor(0xff0000)` — works on next shape rebuild; no auto-recolor of existing shapes (by design)
- [x] 4.5 Manual test: edit `public/config.edn` to `0x00ff00`, hard reload — shapes render green
