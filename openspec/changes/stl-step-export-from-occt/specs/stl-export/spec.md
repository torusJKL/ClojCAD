## ADDED Requirements

### Requirement: Export STL binary file from OCCT shape
The system SHALL export a valid OCCT `TopoDS_Shape` to a binary STL file using `StlAPI_Writer`.

#### Scenario: Export valid shape to STL
- **WHEN** `(kernel/export-stl shape "output.stl")` is called with a valid `TopoDS_Shape` handle and a filename string
- **THEN** it SHALL pre-tessellate the shape via `BRepMesh_IncrementalMesh` with a default `maxDeviation` of 0.05
- **THEN** it SHALL create an OCCT `StlAPI_Writer` instance
- **THEN** it SHALL call `Write_1(shape, filename, Message_ProgressRange_1())` on the writer
- **THEN** it SHALL trigger a browser download of the resulting STL binary file
- **THEN** it SHALL delete the writer and progress objects after use

#### Scenario: Export shape with custom mesh deflection
- **WHEN** `(kernel/export-stl shape "output.stl" {:max-deviation 0.005})` is called
- **THEN** it SHALL apply `BRepMesh_IncrementalMesh` with the given `max-deviation` (0.005) before writing
- **THEN** the STL output SHALL have visibly finer tessellation (rounder sphere, smoother curves)

#### Scenario: Export null/invalid shape
- **WHEN** `(kernel/export-stl nil "output.stl")` is called
- **THEN** it SHALL NOT crash
- **THEN** it SHALL log a warning via `js/console.warn`
- **THEN** it SHALL return nil without triggering a download

#### Scenario: Export shape uses binary STL
- **WHEN** `(kernel/export-stl shape "output.stl")` is called
- **THEN** the STL output SHALL be in binary format (OCCT's `StlAPI_Writer` default; the fork's binding exposes `ASCIIMode()` getter only, no setter)

### Requirement: Downloaded STL filename matches input
The system SHALL use the provided filename for the browser download.

#### Scenario: Download with custom filename
- **WHEN** `(kernel/export-stl shape "my-model.stl")` is called
- **THEN** the browser download prompt SHALL show "my-model.stl" as the suggested filename
