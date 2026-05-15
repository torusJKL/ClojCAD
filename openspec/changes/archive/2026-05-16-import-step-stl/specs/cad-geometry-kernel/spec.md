## ADDED Requirements

### Requirement: Kernel provides STL file import
The system SHALL wrap STL file import via OCCT's `StlAPI_Reader`, converting the resulting shell to a solid via `TopoDS_Cast.Shell_1` and `BRepBuilderAPI_MakeSolid`.

#### Scenario: Import an STL file via kernel API
- **WHEN** `(import-stl file-data filename)` is called
- **THEN** the kernel SHALL create an `StlAPI_Reader` instance
- **THEN** the kernel SHALL write file data to MEMFS
- **THEN** the kernel SHALL call `reader.Read` on the MEMFS path
- **THEN** the kernel SHALL downcast the read shape to `TopoDS_Shell` via `TopoDS_Cast.Shell_1`
- **THEN** the kernel SHALL build a `TopoDS_Solid` via `BRepBuilderAPI_MakeSolid_1`
- **THEN** the kernel SHALL return the resulting `TopoDS_Solid` or nil on failure
- **THEN** the kernel SHALL delete the reader, solid builder, and clean up MEMFS

### Requirement: Kernel provides STEP file import
The system SHALL wrap STEP file import via OCCT's `STEPControl_Reader_1`.

#### Scenario: Import a STEP file via kernel API
- **WHEN** `(import-step file-text filename)` is called
- **THEN** the kernel SHALL create a `STEPControl_Reader_1` instance
- **THEN** the kernel SHALL write file text to MEMFS
- **THEN** the kernel SHALL call `reader.ReadFile` on the MEMFS path
- **THEN** when read succeeds, the kernel SHALL call `reader.TransferRoots`
- **THEN** the kernel SHALL call `reader.OneShape` to get the root shape
- **THEN** the kernel SHALL return the `TopoDS_Shape` or nil on failure
- **THEN** the kernel SHALL delete the reader and clean up MEMFS
