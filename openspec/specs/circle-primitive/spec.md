## Requirements

### Requirement: Kernel provides 2D circle primitive
The system SHALL provide a function to create 2D circles as either wire or face topology.

#### Scenario: Create a circle as a wire
- **WHEN** `(make-circle radius true)` is called with a positive radius
- **THEN** it SHALL create a `TopoDS_Wire` representing the circle outline
- **THEN** it SHALL return the wire shape handle

#### Scenario: Create a circle as a face
- **WHEN** `(make-circle radius false)` is called (or `(make-circle radius)`, defaulting to false)
- **THEN** it SHALL create a `TopoDS_Face` representing the circular planar face
- **THEN** it SHALL return the face shape handle

#### Scenario: Circle rejects invalid radius
- **WHEN** `(make-circle 0)` or `(make-circle -1)` is called
- **THEN** the kernel SHALL return nil

#### Scenario: Circle wire is ready for Loft/Pipe
- **WHEN** a circle is created with `wire?` set to true
- **THEN** the resulting wire SHALL be usable as input to future Loft and Pipe operations
- **THEN** the wire SHALL be a closed, planar wire in the XY plane
