## Requirements

### Requirement: Import STEP files (AP203/AP214)
The system SHALL import STEP files (AP203 and AP214) into OCCT `TopoDS_Shape` objects via `STEPControl_Reader_1`.

#### Scenario: Import STEP file successfully
- **WHEN** the user imports a valid `.step` or `.stp` file
- **THEN** the system SHALL read the file content as text (UTF-8)
- **THEN** the system SHALL write the text to MEMFS via `oc.FS.createDataFile()`
- **THEN** the system SHALL create a `STEPControl_Reader_1` instance
- **THEN** the system SHALL call `reader.ReadFile(memfsPath)`
- **WHEN** `ReadFile` returns `IFSelect_RetDone`
- **THEN** the system SHALL call `reader.TransferRoots(new Message_ProgressRange_1())`
- **THEN** the system SHALL call `reader.OneShape()` to get the root shape
- **THEN** the system SHALL return a valid `TopoDS_Shape`
- **THEN** the system SHALL clean up the MEMFS file

#### Scenario: STEP file read failure
- **WHEN** `ReadFile` does not return `IFSelect_RetDone`
- **THEN** the system SHALL console.warn with the error
- **THEN** the system SHALL return nil
- **THEN** the system SHALL clean up the MEMFS file

#### Scenario: STEP import reader lifecycle cleanup
- **WHEN** a STEP import completes (success or failure)
- **THEN** the system SHALL call `.delete()` on the `STEPControl_Reader_1` instance
- **THEN** the system SHALL call `.delete()` on the `Message_ProgressRange_1` instance
