## Why

The project has 11 test files covering kernel, model, scene, and viewport modules, but several important modules lack tests (export, import, core, demo, viewer, export-ui, import-ui, loading). There is no CI pipeline to run tests automatically, no test coverage measurement, and some existing kernel tests don't run via `npm test` due to WASM module resolution issues. Adding automated tests and CI will improve confidence for future changes and prevent regressions.

## What Changes

- Add GitHub Actions CI workflow to run tests on every push and PR
- Add tests for untested source files: `kernel/export.cljs`, `kernel/import.cljs`, `viewport/loading.cljs`
- Improve test runner to handle WASM-dependent kernel tests reliably in Node.js
- Add `npm run test:watch` script for development feedback loop
- Optionally add coverage reporting

## Capabilities

### New Capabilities
- `ci-automation`: GitHub Actions workflow to compile and run tests automatically
- `kernel-export-test-coverage`: Tests for STL/STEP export functionality
- `kernel-import-test-coverage`: Tests for STEP import functionality
- `viewport-loading-test-coverage`: Tests for loading state management
- `test-runner-improvement`: Fix WASM-dependent test execution in Node.js for automated runs

### Modified Capabilities
<!-- No existing spec-level capabilities are being modified. Test additions don't change product requirements. -->

## Impact

- `.github/workflows/test.yml` — new CI workflow file
- `test/ClojCAD/kernel/export_test.cljs` — new test file
- `test/ClojCAD/kernel/import_test.cljs` — new test file
- `test/ClojCAD/viewport/loading_test.cljs` — new test file
- `test/clojcad/runner.cljs` — may need updating to include WASM init before kernel tests
- `package.json` — may add `test:watch` script
- `shadow-cljs.edn` — may need test build config adjustments for WASM support
