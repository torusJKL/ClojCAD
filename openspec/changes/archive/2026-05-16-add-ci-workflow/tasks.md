## 1. CI Automation

- [x] 1.1 Create `.github/workflows/test.yml` with Node.js setup, shadow-cljs compilation, and test execution on push/PR
- [x] 1.2 Add `test:watch` script to `package.json` using `shadow-cljs watch test`

## 2. Test Runner Improvement

- [x] 2.1 Update `test/clojcad/runner.cljs` to include WASM-dependent kernel test namespaces (init-test, primitives-test, booleans-test, mesh-test) with graceful fallback when WASM unavailable
- [x] 2.2 Add WASM availability detection helper function in runner that prints skip warnings

## 3. Kernel Export Tests

- [x] 3.1 Create `test/ClojCAD/kernel/export_test.cljs` with tests for `export-stl` (valid shape, invalid shape) and `export-step` (valid single shape, valid multiple shapes, invalid shape) guarded by WASM availability

## 4. Kernel Import Tests

- [x] 4.1 Create `test/ClojCAD/kernel/import_test.cljs` with tests for `import-stl` (valid binary data, invalid data) and `import-step` (valid STEP text, invalid text) guarded by WASM availability

## 5. Viewport Loading Tests

- [x] 5.1 Create `test/ClojCAD/viewport/loading_test.cljs` with DOM-mocked tests for `show-loading!`, `hide-loading!` idempotency, and `notify!` toast behavior

## 6. Integration

- [x] 6.1 Update test runner to include new export-test, import-test, and loading-test namespaces
- [x] 6.2 Verify `npm test` passes with all existing and new tests
- [x] 6.3 Verify CI workflow runs successfully on a test branch/push (requires push to GitHub)
