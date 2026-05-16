## ADDED Requirements

### Requirement: Export module functions are tested
The system SHALL have tests covering the `ClojCAD.kernel.export` namespace for both STL and STEP export scenarios.

#### Scenario: export-stl with a valid shape produces output
- **WHEN** a valid `TopoDS_Shape` (e.g., a sphere) is passed to `export-stl` with a filename
- **THEN** the function does not throw an error
- **THEN** `download-blob!` is called (or the export pipeline completes without failure)

#### Scenario: export-stl with nil shape logs warning
- **WHEN** `nil` is passed to `export-stl`
- **THEN** `js/console.warn` is called with a message containing "invalid shape"
- **THEN** no export is attempted

#### Scenario: export-stl with null shape logs warning
- **WHEN** a null/IsNull shape is passed to `export-stl`
- **THEN** `js/console.warn` is called with a message containing "invalid shape"

#### Scenario: export-step with a valid shape produces output
- **WHEN** a valid `TopoDS_Shape` is passed to `export-step` with a filename
- **THEN** the function does not throw an error
- **THEN** the export pipeline completes without failure

#### Scenario: export-step with multiple shapes works
- **WHEN** a vector of valid shapes is passed to `export-step`
- **THEN** all shapes are transferred to the STEP writer
- **THEN** the function completes without failure

#### Scenario: export-step with invalid shape logs warning
- **WHEN** a vector containing a nil shape is passed to `export-step`
- **THEN** `js/console.warn` is called with a message containing "invalid shape"
