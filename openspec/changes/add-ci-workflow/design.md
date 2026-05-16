## Context

ClojCAD is a ClojureScript + OpenCascade.js parametric CAD application. Tests use `cljs.test` and run via Node.js (shadow-cljs `:node-script` target). There are 11 existing test files but several modules lack coverage (export, import, loading). No CI pipeline exists. Some kernel tests depend on WASM (OpenCascade.js) and are excluded from the test runner.

## Goals / Non-Goals

**Goals:**
- GitHub Actions CI workflow to compile and run tests automatically on push/PR
- Tests for `kernel/export.cljs` (STL/STEP export)
- Tests for `kernel/import.cljs` (STL/STEP import)
- Tests for `viewport/loading.cljs` (loading overlay and toast notification)
- Improved test runner that includes WASM-dependent tests with graceful fallback
- Development feedback loop with `test:watch` script

**Non-Goals:**
- Browser-based (e2e) testing — not practical for this project's scope
- Coverage reporting tooling — optional, can be added later
- Refactoring production code for testability — minimal changes only
- Tests for `core.cljs`, `demo.cljs`, `viewer.cljs`, `export-ui.cljs`, `import-ui.cljs` — these are entry points/DOM-heavy and not practical to test in Node.js without major refactoring

## Decisions

1. **CI Platform: GitHub Actions**: Standard for open-source projects. Free tier sufficient. Simple Node.js setup matches existing `npm test` script.

2. **WASM-dependent tests in runner**: Add WASM-dependent test namespaces to the runner but guard them behind an `init-kernel` check. The `init.cljs` module already exposes `oc-instance` atom — if WASM fails to load (e.g., in some Node.js environments), tests gracefully skip. The `booleans_test.cljs` comment says "skipped automatically when WASM init fails" — we should make this explicit with `if-let` or `when-some` guards.

3. **Export/import tests**: These depend on the loaded OCCT WASM instance. Write tests that:
   - Skip gracefully if WASM not available (via `init/oc-instance`)
   - Create primitive shapes, export to STL/STEP, verify output is non-nil
   - Import STL/STEP data, verify returned shape is non-null and not `IsNull`
   - These are integration-level tests that exercise the full pipeline

4. **Loading tests**: The `loading.cljs` module is entirely DOM-dependent. Approach:
   - Test the module's state management (atoms `*injected-style`, `*overlay`) via mocking DOM APIs with simple js-object mocks
   - Verify `show-loading!` and `hide-loading!` transitions correctly
   - Verify `notify!` creates/removes a toast element
   - Use Node.js-compatible DOM mocking (simple redefinitions, no jsdom dependency)

5. **Test watch script**: Use `shadow-cljs watch test` for continuous compilation, paired with a Node.js file watcher (e.g., `nodemon` or `node --watch`) to re-run on changes.

6. **CI workflow**:
   - `npm ci` (clean install)
   - `npx shadow-cljs compile test`
   - `node target/test/runner.js`
   - Run on push to main and pull requests

## Risks / Trade-offs

- **WASM in Node.js**: [Risk] OpenCascade.js WASM may fail to resolve in some Node.js/GitHub Actions runner environments. [Mitigation] Tests degrade gracefully — skip with warning rather than fail. The runner detects WASM availability and reports skipped tests.
- **DOM mocking**: [Risk] Mocking DOM APIs for loading tests may not perfectly reflect browser behavior. [Mitigation] Keep tests focused on state transitions and element creation logic, not visual rendering.
- **Export tests write to filesystem**: [Risk] File system interactions could be flaky in CI. [Mitigation] Export tests use the OCCT in-memory filesystem (memfs), not the real filesystem — no cleanup needed.
- **CI dependency on WASM binary**: [Risk] `cascadestudio.wasm` (~30MB) must be present for WASM tests. [Mitigation] `postinstall` script already copies it to `public/`. CI workflow ensures this runs.
