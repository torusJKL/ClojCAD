## ADDED Requirements

### Requirement: Import module functions are tested
The system SHALL have tests covering the `ClojCAD.kernel.import` namespace for both STL and STEP import scenarios.

#### Scenario: import-stl with valid binary STL data returns a shape
- **WHEN** valid binary STL data (ArrayBuffer) is passed to `import-stl`
- **THEN** a non-nil `TopoDS_Shape` is returned
- **THEN** the returned shape has `IsNull` returning false

#### Scenario: import-stl with invalid data returns nil
- **WHEN** corrupted/invalid data is passed to `import-stl`
- **THEN** `nil` is returned
- **THEN** a warning is logged

#### Scenario: import-step with valid STEP text returns a shape
- **WHEN** valid STEP text is passed to `import-step`
- **THEN** a non-nil `TopoDS_Shape` is returned
- **THEN** the returned shape has `IsNull` returning false

#### Scenario: import-step with invalid text returns nil
- **WHEN** invalid text is passed to `import-step`
- **THEN** `nil` is returned
- **THEN** a warning is logged

#### Scenario: memfs is cleaned up after import
- **WHEN** an import completes (success or failure)
- **THEN** the temporary file in the OCCT in-memory filesystem is removed
