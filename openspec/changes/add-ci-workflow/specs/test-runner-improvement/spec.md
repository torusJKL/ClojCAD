## ADDED Requirements

### Requirement: Test runner includes WASM-dependent test namespaces
The test runner SHALL include WASM-dependent kernel test namespaces (init, primitives, booleans, mesh) with graceful fallback when WASM is unavailable.

#### Scenario: WASM-dependent tests run when WASM is available
- **WHEN** the test runner executes in an environment where `ClojCAD.kernel.init/init-kernel` succeeds
- **THEN** WASM-dependent test namespaces (init-test, primitives-test, booleans-test, mesh-test) are included in test execution
- **THEN** their test results are reported normally

#### Scenario: WASM-dependent tests are skipped when WASM is unavailable
- **WHEN** the test runner executes in an environment where `ClojCAD.kernel.init/init-kernel` fails (e.g., missing WASM binary)
- **THEN** WASM-dependent test namespaces are skipped
- **THEN** a warning message is printed indicating which tests were skipped
- **THEN** non-WASM tests continue to run and report normally

### Requirement: Test runner includes new export and import test namespaces
The test runner SHALL include the new `ClojCAD.kernel.export-test` and `ClojCAD.kernel.import-test` namespaces.

#### Scenario: Export and import tests are included in runner
- **WHEN** `npm test` is executed
- **THEN** `ClojCAD.kernel.export-test` and `ClojCAD.kernel.import-test` namespaces are required and executed
- **THEN** their results are reported alongside existing tests

### Requirement: Test runner includes loading test namespace
The test runner SHALL include the new `ClojCAD.viewport.loading-test` namespace.

#### Scenario: Loading tests are included in runner
- **WHEN** `npm test` is executed
- **THEN** `ClojCAD.viewport.loading-test` namespace is required and executed
- **THEN** its test results are reported alongside existing tests

### Requirement: Test watch script for development
The system SHALL provide a `test:watch` npm script for continuous test execution during development.

#### Scenario: Test watch recompiles and re-runs on source changes
- **WHEN** a source or test file changes
- **THEN** shadow-cljs recompiles the test build
- **THEN** the compiled runner executes the test suite
- **THEN** results are printed to the terminal
