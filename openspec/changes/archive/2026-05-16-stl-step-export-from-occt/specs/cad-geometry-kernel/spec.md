## ADDED Requirements

### Requirement: Kernel initializes export-related OCCT classes
The system SHALL load StlAPI_Writer and STEPControl_Writer classes as part of the OCCT WASM initialization. These classes are available in the rebuilt opencascade.js fork and do not require additional async loading.

#### Scenario: Export classes available after init
- **WHEN** the kernel finishes initialization
- **THEN** `oc.StlAPI_Writer` SHALL be a valid constructor
- **THEN** `oc.STEPControl_Writer` SHALL be a valid constructor
- **THEN** `oc.STEPControl_StepDataVersion` SHALL have `STEPControl_AsIs` defined

### Requirement: Export function result does not leak OCCT objects
The system SHALL properly clean up all OCCT objects allocated during export.

#### Scenario: Writer objects are deleted after use
- **WHEN** `(kernel/export-stl ...)` or `(kernel/export-step ...)` completes (success or failure)
- **THEN** the `StlAPI_Writer` or `STEPControl_Writer` instance SHALL be deleted via `.delete()`
- **THEN** any intermediate OCCT objects (strings, transfer processes) SHALL be deleted
