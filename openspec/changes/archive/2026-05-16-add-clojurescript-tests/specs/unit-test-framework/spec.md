## ADDED Requirements

### Requirement: Test suite runs via shadow-cljs node-test target
The system SHALL provide a ClojureScript test suite that runs via shadow-cljs's `:target :node-test` build configuration.

#### Scenario: Run tests from command line
- **WHEN** user runs `npx shadow-cljs compile :test`
- **THEN** shadow-cljs SHALL compile test files along with all source dependencies
- **THEN** the compiled test runner SHALL execute all `deftest` forms
- **THEN** test results (pass/fail/error counts) SHALL be printed to stdout

#### Scenario: Test runner exits with nonzero on failure
- **WHEN** any test assertion fails
- **THEN** the test runner SHALL exit with a non-zero status code

### Requirement: Test source directory mirrors source structure
The system SHALL organize test files in a `test/` directory that mirrors the `src/` directory structure.

#### Scenario: Test file naming convention
- **WHEN** a source file exists at `src/ClojCAD/kernel/primitives.cljs`
- **THEN** its tests SHALL be at `test/ClojCAD/kernel/primitives_test.cljs`
- **THEN** the test namespace SHALL follow the pattern `ClojCAD.kernel.primitives-test`

### Requirement: npm test script
The system SHALL provide a `test` entry in `package.json` that runs the full test suite.

#### Scenario: npm test invocation
- **WHEN** user runs `npm test`
- **THEN** it SHALL compile and run all ClojureScript tests via shadow-cljs
- **THEN** results SHALL be printed to stdout
