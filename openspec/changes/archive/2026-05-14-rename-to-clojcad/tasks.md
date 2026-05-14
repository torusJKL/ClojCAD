## 1. Rename Source Directory

- [x] 1.1 Rename `src/CADscript/` to `src/ClojCAD/` using `git mv src/CADscript src/ClojCAD`

## 2. Update Namespace Declarations

- [x] 2.1 Update `ns` form in `src/ClojCAD/core.cljs`: `CADscript.core` → `ClojCAD.core`
- [x] 2.2 Update `ns` form in `src/ClojCAD/demo.cljs`: `CADscript.demo` → `ClojCAD.demo`
- [x] 2.3 Update `ns` form in `src/ClojCAD/kernel/api.cljs`: `CADscript.kernel.api` → `ClojCAD.kernel.api`
- [x] 2.4 Update `ns` form in `src/ClojCAD/kernel/init.cljs`: `CADscript.kernel.init` → `ClojCAD.kernel.init`
- [x] 2.5 Update `ns` form in `src/ClojCAD/kernel/lifecycle.cljs`: `CADscript.kernel.lifecycle` → `ClojCAD.kernel.lifecycle`
- [x] 2.6 Update `ns` form in `src/ClojCAD/kernel/mesh.cljs`: `CADscript.kernel.mesh` → `ClojCAD.kernel.mesh`
- [x] 2.7 Update `ns` form in `src/ClojCAD/kernel/primitives.cljs`: `CADscript.kernel.primitives` → `ClojCAD.kernel.primitives`
- [x] 2.8 Update `ns` form in `src/ClojCAD/model/core.cljs`: `CADscript.model.core` → `ClojCAD.model.core`
- [x] 2.9 Update `ns` form in `src/ClojCAD/model/defmodel.clj`: `CADscript.model.defmodel` → `ClojCAD.model.defmodel`
- [x] 2.10 Update `ns` form in `src/ClojCAD/model/registry.cljs`: `CADscript.model.registry` → `ClojCAD.model.registry`
- [x] 2.11 Update `ns` form in `src/ClojCAD/model/tag.cljs`: `CADscript.model.tag` → `ClojCAD.model.tag`
- [x] 2.12 Update `ns` form in `src/ClojCAD/scene/manager.cljs`: `CADscript.scene.manager` → `ClojCAD.scene.manager`
- [x] 2.13 Update `ns` form in `src/ClojCAD/viewport/viewer.cljs`: `CADscript.viewport.viewer` → `ClojCAD.viewport.viewer`
- [x] 2.14 Update `ns` form in `src/ClojCAD/viewport/shape_adapter.cljs`: `CADscript.viewport.shape-adapter` → `ClojCAD.viewport.shape-adapter`

## 3. Update Namespace References

- [x] 3.1 Update all `:require` references in `src/ClojCAD/core.cljs`: replace `CADscript.` prefix with `ClojCAD.`
- [x] 3.2 Update all `:require` references in `src/ClojCAD/demo.cljs`: replace `CADscript.` prefix with `ClojCAD.`
- [x] 3.3 Update all `:require` references in `src/ClojCAD/kernel/api.cljs`: replace `CADscript.` prefix with `ClojCAD.`
- [x] 3.4 Update `js*` interop reference in `src/ClojCAD/kernel/mesh.cljs`: `CADscript.kernel.init` → `ClojCAD.kernel.init`
- [x] 3.5 Update `:require` references in `src/ClojCAD/kernel/primitives.cljs`: replace `CADscript.` prefix with `ClojCAD.`
- [x] 3.6 Update `:require` references in `src/ClojCAD/model/core.cljs`: replace `CADscript.` prefix with `ClojCAD.`
- [x] 3.7 Update fully-qualified symbol in `src/ClojCAD/model/defmodel.clj`: `CADscript.model.core/reactive-model` → `ClojCAD.model.core/reactive-model`
- [x] 3.8 Update `:require` references in `src/ClojCAD/scene/manager.cljs`: replace `CADscript.` prefix with `ClojCAD.`

## 4. Update Build Configuration

- [x] 4.1 Update `shadow-cljs.edn`: change `:init-fn CADscript.core/init` to `ClojCAD.core/init`

## 5. Verification

- [x] 5.1 Run `npx shadow-cljs compile dev` — ClojureScript compilation phase succeeds (Closure error in `cascadestudio.js` is pre-existing, confirmed identical before and after rename)
- [x] 5.2 Verify no `CADscript` references remain in source: `rg CADscript src/ shadow-cljs.edn` returns no matches
