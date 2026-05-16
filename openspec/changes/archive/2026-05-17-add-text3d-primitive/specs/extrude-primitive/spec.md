## ADDED Requirements

### Requirement: Kernel provides linear extrusion
The system SHALL provide an `extrude` function that linearly extrudes a face into a solid along a direction vector.

#### Scenario: Extrude a face along Z
- **WHEN** `(extrude face [0 0 10])` is called with a valid `TopoDS_Face`
- **THEN** it SHALL create a prism via `BRepPrimAPI_MakePrism`
- **THEN** it SHALL return a `TopoDS_Solid` shape handle
- **THEN** the resulting solid SHALL extend 10 units in the Z direction

#### Scenario: Extrude a face along arbitrary direction
- **WHEN** `(extrude face [5 5 0])` is called
- **THEN** the resulting solid SHALL extend along the vector (5, 5, 0)

#### Scenario: Extrude nil face returns nil
- **WHEN** `(extrude nil [0 0 10])` is called
- **THEN** it SHALL return nil
- **THEN** it SHALL NOT throw an exception

#### Scenario: Extruded shape is lifecycle-tracked
- **WHEN** `(extrude face [0 0 10])` is called
- **THEN** the resulting shape SHALL be registered in the lifecycle tracker
