## Why

The ClojCAD project has zero tests despite having a growing codebase with 15+ namespaces covering geometry kernel operations, model lifecycle, scene management, and UI integration. Without tests, regressions go undetected, refactoring is risky, and the project lacks a safety net for ongoing development. Adding test coverage now — before the codebase grows further — establishes foundational quality practices.

## What Changes

- Introduce a ClojureScript test runner configuration (shadow-cljs node-test target)
- Add unit tests for all kernel-layer namespaces (primitives, booleans, mesh, lifecycle, export, import)
- Add unit tests for the model layer (core, registry, tag)
- Add unit tests for the scene manager (excluding DOM/viewer integration)
- Add unit tests for shape-adapter data transformations
- Add unit tests for viewport config (pure functions only)
- Add a `npm test` script to run the test suite
- Document how to write and run tests in the project README

## Capabilities

### New Capabilities

- `unit-test-framework`: Test runner setup, configuration, and CI integration for running ClojureScript tests
- `kernel-test`: Tests for geometry kernel namespaces (primitives, booleans, mesh, lifecycle, export, import)
- `model-test`: Tests for model definition, registry, and tagging namespaces
- `scene-manager-test`: Tests for scene manager logic (params watch, show/hide/remove, set-opacity) — isolated from the DOM viewer dependency

### Modified Capabilities

- `cad-geometry-kernel`: Add testability requirements — each public function must be verifiable through its API contract
- `parametric-model`: Add testability requirements — the `defmodel` macro and `reactive-model` must produce verifiable model behavior

## Impact

- `shadow-cljs.edn` — new `:test` build configuration with `:target :node-test`
- `package.json` — new `"test"` script entry
- `src/` — no changes to production code (test files live in a separate `test/` directory)
- New directory `test/clojcad/` mirroring source structure for test files
- New dev dependency on `cljs.test` (included with ClojureScript, no extra dep)
