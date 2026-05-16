## ADDED Requirements

### Requirement: Defmodel macro generates testable model functions
The `defmodel` macro SHALL produce functions that can be tested independently of the viewer/display environment.

#### Scenario: Model function returns shape without viewer
- **WHEN** a model defined via `defmodel` is called with valid parameters
- **THEN** the result SHALL be a map with at least a `:shape` key
- **THEN** `:shape` SHALL be a non-nil OCCT shape

#### Scenario: Model function propagates errors cleanly
- **WHEN** a model function encounters an error during evaluation
- **THEN** the error SHALL be catchable and not crash the runtime

### Requirement: Reactive model caching is testable
The caching behavior of `reactive-model` SHALL be directly testable by calling the returned function.

#### Scenario: Cache returns identical value for same params
- **WHEN** a `reactive-model` is called twice with identical parameters
- **THEN** the result of the second call SHALL satisfy `identical?` with the first call result
