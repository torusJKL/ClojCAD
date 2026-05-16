## Context

ClojCAD is a browser-based parametric CAD in ClojureScript with 15+ source namespaces, zero test coverage. The codebase has three architectural layers:

1. **Kernel** (`ClojCAD.kernel.*`) — wraps OpenCascade.js WASM geometry library. Depends on OCCT WASM runtime. Contains primitives, booleans, mesh tessellation, export, import, and lifecycle management.
2. **Model** (`ClojCAD.model.*`) — pure ClojureScript (except `defmodel` which is a `.clj` macro). No external dependencies. Contains model registry, reactive caching, and tag system.
3. **Scene/Viewport** (`ClojCAD.scene.*`, `ClojCAD.viewport.*`) — manages display state and integrates with the three-cad-viewer DOM library. Depends on browser APIs.

Testing each layer requires different strategies due to their dependency profiles.

## Goals / Non-Goals

**Goals:**
- Establish a test runner using shadow-cljs's `:target :node-test` (Node.js-based, no browser needed)
- Achieve unit test coverage for all namespaces that can be tested without a browser
- Test kernel operations by running OCCT WASM in Node.js (OpenCascade.js supports this)
- Test model layer as pure ClojureScript functions
- Test scene manager logic (params atom, scene atom operations) by mocking the viewer dependency
- Provide `npm test` command for running the suite
- Create a `test/` directory mirroring `src/` structure

**Non-Goals:**
- Browser-based or integration tests (e.g., Selenium, playwright)
- UI component tests for the viewer toolbar (export/import buttons, loading overlay)
- Visual regression tests for rendered 3D output
- CI pipeline configuration (covered separately)
- Performance benchmarks

## Decisions

### 1. Test Runner: shadow-cljs node-test target

- **Decision**: Use `shadow-cljs` built-in `:target :node-test` instead of `doo` or `olical/cljs-test-runner`.
- **Rationale**: `:target :node-test` is built into shadow-cljs, requires no extra dependencies, supports `cljs.test` natively, and runs on Node.js with full access to npm packages (including OpenCascade.js). Alternative runners like `doo` add a Karma/browser dependency that is unnecessary since kernel code runs in Node.js.

### 2. Test framework: cljs.test

- **Decision**: Use `cljs.test` (bundled with ClojureScript) as the testing framework.
- **Rationale**: Zero additional dependencies, idiomatic ClojureScript, familiar to Clojure developers, and fully supported by shadow-cljs.

### 3. Test file organization: Mirror src/ structure

- **Decision**: Test files live in `test/ClojCAD/<path>_test.cljs` mirroring `src/ClojCAD/<path>.cljs`.
- **Rationale**: Convention in Clojure ecosystem. Makes it easy to find corresponding tests. Shadow-cljs source-paths config includes both `src` and `test`.

### 4. Viewer/Scene test isolation: Dynamic var mocking

- **Decision**: For `scene.manager` tests that interact with the viewer, bind `vw/*viewer` to nil and test only the atom state changes (scene atom, model visibility, opacity). The actual viewer interaction paths are tested implicitly through atom assertions.
- **Rationale**: The viewer is a complex JS object requiring DOM. The scene manager's logic is primarily about managing the `scene` atom. The few code paths that call viewer methods are trivial one-liners. Testing the atom state gives us ~90% coverage of the logic.

### 5. Kernel test setup: Shared WASM initialization

- **Decision**: Use `:once` fixtures in `cljs.test` to initialize the OCCT WASM module once before all kernel tests.
- **Rationale**: WASM init takes ~1-2 seconds and the module is stateless after init. `:once` fixtures avoid per-test initialization cost.

### 6. Kernel test separation: Integration-style tests

- **Decision**: Kernel tests are integration-style — they exercise real OCCT operations. No mocking of the WASM layer.
- **Rationale**: The kernel layer IS the integration with OCCT. Mocking would test our assumptions, not the actual behavior. OpenCascade.js works in Node.js, so we can test the real thing.

## Risks / Trade-offs

- **[WASM in Node.js] OpenCascade.js WASM initialization may behave differently in Node.js vs browser** → Mitigation: Test with a small set of representative shapes first. OCCT operations are deterministic and should produce identical results.
- **[Test speed] WASM init adds ~2s to test startup** → Mitigation: Use `:once` fixture so it runs once per namespace. Acceptable trade-off for real integration coverage.
- **[Fragile tests] Kernel tests depend on specific OCCT behavior (shape types, tessellation output)** → Mitigation: Test properties (result is non-nil, result is a shape, typed array lengths) rather than exact coordinate values. OCCT internal changes would mainly affect numerical results, not structural properties.
- **[Viewer isolation] Scene manager tests with nil viewer don't test viewer interaction paths** → Mitigation: These paths are thin delegation calls. If the viewer API changes, compilation errors will catch it. Not worth the complexity of mocking a complex JS object.
