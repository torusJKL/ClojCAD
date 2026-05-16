## 1. Test Framework Setup

- [x] 1.1 Add `:test` build configuration to `shadow-cljs.edn` with `:target :node-script` and source paths including both `src` and `test`
- [x] 1.2 Create `test/ClojCAD` directory structure mirroring `src/ClojCAD`
- [x] 1.3 Create a test runner entry point at `test/clojcad/runner.cljs` that requires all test namespaces
- [x] 1.4 Add `"test": "npx shadow-cljs compile test && node target/test/runner.js"` script to `package.json`
- [x] 1.5 Verify the test runner works by creating a trivial passing test

## 2. Kernel Lifecycle Tests

- [x] 2.1 Create `test/ClojCAD/kernel/lifecycle_test.cljs` with `track` tests (adds to atom, returns object)
- [x] 2.2 Add `destroy` tests (calls .delete, removes from atom)
- [x] 2.3 Add `destroy-all` tests (clears all tracked objects, calls .delete on each)

## 3. Model Layer Tests

- [x] 3.1 Create `test/ClojCAD/model/registry_test.cljs` with tests for `register!`, `lookup`, and `registered-keys`
- [x] 3.2 Create `test/ClojCAD/model/tag_test.cljs` with tests for dynamic binding context behavior
- [x] 3.3 Create `test/ClojCAD/model/core_test.cljs` with tests for reactive-model caching (same params returns cached, different params recomputes)

## 4. Kernel Primitive Tests (WASM-dependent)

- [x] 4.1 Create `test/ClojCAD/kernel/init_test.cljs` with tests for OCCT WASM initialization
- [x] 4.2 Create `test/ClojCAD/kernel/primitives_test.cljs` with tests for `make-sphere`, `make-box`, `make-cylinder`, `make-cone`
- [x] 4.3 Add tests for `translate` and `rotate` transform functions

## 5. Kernel Boolean Operation Tests

- [x] 5.1 Create `test/ClojCAD/kernel/booleans_test.cljs` with tests for `fuse` on overlapping shapes
- [x] 5.2 Add tests for `cut` and `common` operations
- [x] 5.3 Add tests for non-overlapping `common` returning nil
- [x] 5.4 Add tests for variadic chaining (3+ arguments)

## 6. Kernel Tessellation Tests

- [x] 6.1 Create `test/ClojCAD/kernel/mesh_test.cljs` with test verifying `tessellate` returns all expected keys
- [x] 6.2 Add test verifying output types (Float32Array, Uint32Array)
- [x] 6.3 Add test for custom `maxDeviation` parameter

## 7. Scene Manager Tests

- [x] 7.1 Create `test/ClojCAD/scene/manager_test.cljs` with tests for `params` atom initial state
- [x] 7.2 Add tests for show/hide/remove model updating scene atom state
- [x] 7.3 Add tests for `set-opacity` updating scene atom

## 8. Viewport Utility Tests

- [x] 8.1 Create `test/ClojCAD/viewport/shape_adapter_test.cljs` with tests for `tessellation->shape` conversion
- [x] 8.2 Create `test/ClojCAD/viewport/config_test.cljs` with tests for `parse-color` and `get-default-shape-color`

## 9. Documentation

- [x] 9.1 Add testing section to `README.org` covering how to run tests and write new ones
