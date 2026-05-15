## ADDED Requirements

### Requirement: Export STEP AP203 file from OCCT shape
The system SHALL export a valid OCCT `TopoDS_Shape` to a STEP AP203 file using `STEPControl_Writer`.

#### Scenario: Export valid shape to STEP
- **WHEN** `(kernel/export-step shape "output.step")` is called with a valid `TopoDS_Shape` handle and a filename string
- **THEN** it SHALL create an OCCT `STEPControl_Writer` instance (constructor `STEPControl_Writer_1`)
- **THEN** it SHALL call `Transfer_1(shape, STEPControl_StepModelType.STEPControl_AsIs, true, Message_ProgressRange_1())` to transfer the shape
- **THEN** it SHALL call `.Write(filename)` to write the STEP file to MEMFS
- **THEN** it SHALL read the file from MEMFS and trigger a browser download
- **THEN** it SHALL clean up the MEMFS file via `FS.unlink`
- **THEN** it SHALL delete the writer object after use

#### Scenario: Export multiple shapes as a single STEP file
- **WHEN** `(kernel/export-step [shape-a shape-b] "assembly.step")` is called with a vector of `TopoDS_Shape` handles
- **THEN** it SHALL transfer each shape sequentially via `.Transfer()`
- **THEN** the STEP file SHALL contain all shapes as a compound or separate entities

#### Scenario: Export null/invalid shape
- **WHEN** `(kernel/export-step nil "output.step")` is called
- **THEN** it SHALL NOT crash
- **THEN** it SHALL log a warning via `js/console.warn`
- **THEN** it SHALL return nil without triggering a download

#### Scenario: Transfer failure for unsupported shape type
- **WHEN** `(kernel/export-step shape "output.step")` is called with a shape that OCCT cannot transfer to STEP (e.g., an incomplete compound)
- **THEN** it SHALL check the return status of `.Transfer()`
- **THEN** if transfer fails, it SHALL log a warning and NOT trigger a download

### Requirement: STEP header includes application metadata
The system SHALL set basic STEP header fields for traceability.

#### Scenario: Default header values
- **WHEN** `(kernel/export-step shape "output.step")` is called
- **THEN** the STEP header SHALL include "ClojCAD" as the originating system
- **THEN** the STEP header SHALL include the current date/time as the timestamp

### Requirement: Downloaded STEP filename matches input
The system SHALL use the provided filename for the browser download.

#### Scenario: Download with custom filename
- **WHEN** `(kernel/export-step shape "my-model.step")` is called
- **THEN** the browser download prompt SHALL show "my-model.step" as the suggested filename
