## ADDED Requirements

### Requirement: CI runs tests automatically on push and PR
The system SHALL run the full test suite automatically via GitHub Actions on every push to main and every pull request.

#### Scenario: Tests pass on push to main
- **WHEN** a commit is pushed to the `main` branch
- **THEN** GitHub Actions triggers a workflow that runs `npm test`
- **THEN** the workflow reports a green checkmark on the commit

#### Scenario: Tests run on pull request
- **WHEN** a pull request is opened or updated
- **THEN** GitHub Actions triggers a workflow that runs `npm test`
- **THEN** the workflow status is reported on the PR

#### Scenario: Workflow fails on test failure
- **WHEN** a test fails during the CI workflow
- **THEN** the workflow reports failure (red X)
- **THEN** the test output is available in the workflow logs

### Requirement: CI environment has WASM binary available
The CI environment SHALL ensure `cascadestudio.wasm` is present for WASM-dependent kernel tests.

#### Scenario: WASM binary is copied during install
- **WHEN** `npm ci` runs in CI
- **THEN** the `postinstall` script copies `cascadestudio.wasm` to `public/`
- **THEN** WASM-dependent tests can locate the binary
