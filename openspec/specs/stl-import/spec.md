## Requirements

### Requirement: Import STL files (ASCII and binary)
The system SHALL import both ASCII and binary STL files into OCCT `TopoDS_Solid` objects via `StlAPI_Reader`, `TopoDS_Cast`, and `BRepBuilderAPI_MakeSolid`.

#### Scenario: Import ASCII STL file
- **WHEN** the user imports a valid ASCII `.stl` file
- **THEN** the system SHALL read the file content as binary data
- **THEN** the system SHALL write the data to MEMFS via `oc.FS.createDataFile()`
- **THEN** the system SHALL create an `StlAPI_Reader` instance
- **THEN** the system SHALL call `reader.Read(shape, memfsPath)`
- **THEN** the system SHALL downcast the resulting shape to `TopoDS_Shell` via `TopoDS_Cast.Shell_1`
- **THEN** the system SHALL create a solid via `BRepBuilderAPI_MakeSolid_1` + `.Add(shell)` + `.Solid()`
- **THEN** the system SHALL return a valid `TopoDS_Solid` on success
- **THEN** the system SHALL clean up the MEMFS file via `oc.FS.unlink()`

#### Scenario: Import binary STL file
- **WHEN** the user imports a valid binary `.stl` file
- **THEN** the system SHALL read the file content as `ArrayBuffer`
- **THEN** the system SHALL write the data to MEMFS as a `Uint8Array`
- **THEN** the system SHALL call `reader.Read(shape, memfsPath)`
- **THEN** the system SHALL downcast and convert to solid via `TopoDS_Cast` + `BRepBuilderAPI_MakeSolid`
- **THEN** the system SHALL return a valid `TopoDS_Solid` on success

#### Scenario: STL import with invalid data
- **WHEN** the user imports a corrupt or invalid `.stl` file
- **THEN** the system SHALL detect the reader failure
- **THEN** the system SHALL console.warn with the error
- **THEN** the system SHALL return nil
- **THEN** the system SHALL clean up the MEMFS file

#### Scenario: STL import reader lifecycle cleanup
- **WHEN** an STL import completes (success or failure)
- **THEN** the system SHALL call `.delete()` on the `StlAPI_Reader` instance
