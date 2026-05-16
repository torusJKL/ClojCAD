## ADDED Requirements

### Requirement: Kernel provides cylinder primitive
The system SHALL wrap opencascade.js `BRepPrimAPI_MakeCylinder` with an optional `centered?` parameter.

#### Scenario: Create a cylinder
- **WHEN** the kernel function `(make-cylinder radius height)` is called
- **THEN** it SHALL create an OC cylinder with the given radius and height via `BRepPrimAPI_MakeCylinder`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a centered cylinder
- **WHEN** `(make-cylinder radius height true)` is called
- **THEN** the cylinder SHALL be centered at the origin along its Z axis

### Requirement: Kernel provides cone primitive
The system SHALL wrap opencascade.js `BRepPrimAPI_MakeCone`.

#### Scenario: Create a cone
- **WHEN** the kernel function `(make-cone radius1 radius2 height)` is called
- **THEN** it SHALL create an OC cone via `BRepPrimAPI_MakeCone`
- **THEN** it SHALL return the OC shape handle

### Requirement: Kernel provides circle primitive
The system SHALL provide a 2D circle primitive via OCCT edge/wire/face builders.

#### Scenario: Create a circle wire
- **WHEN** `(make-circle radius true)` is called
- **THEN** it SHALL create a `TopoDS_Wire` via OCCT edge and wire builders
- **THEN** it SHALL return the wire shape handle

#### Scenario: Create a circle face
- **WHEN** `(make-circle radius false)` or `(make-circle radius)` is called
- **THEN** it SHALL create a `TopoDS_Face` via OCCT wire and face builders
- **THEN** it SHALL return the face shape handle

### Requirement: Kernel provides polygon primitive
The system SHALL provide a 2D polygon primitive via OCCT `BRepBuilderAPI_MakePolygon`.

#### Scenario: Create a polygon wire
- **WHEN** `(make-polygon points true)` is called with at least 3 points
- **THEN** it SHALL create a closed `TopoDS_Wire` via `BRepBuilderAPI_MakePolygon`
- **THEN** it SHALL return the wire shape handle

#### Scenario: Create a polygon face
- **WHEN** `(make-polygon points false)` or `(make-polygon points)` is called
- **THEN** it SHALL create a `TopoDS_Face` via OCCT face builder from the polygon wire
- **THEN** it SHALL return the face shape handle

## MODIFIED Requirements

### Requirement: Kernel provides primitive shape creation
The system SHALL wrap opencascade.js primitive creation functions.

#### Scenario: Create a sphere
- **WHEN** the kernel function `(make-sphere radius)` is called
- **THEN** it SHALL create an OC sphere with the given radius via `BRepPrimAPI_MakeSphere`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a box
- **WHEN** the kernel function `(make-box width depth height)` or `(make-box width depth height false)` is called
- **THEN** it SHALL create an OC box via `BRepPrimAPI_MakeBox`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a centered box
- **WHEN** `(make-box width depth height true)` is called
- **THEN** it SHALL create a box centered at the origin
- **THEN** it SHALL extend from `(-width/2, -depth/2, -height/2)` to `(width/2, depth/2, height/2)`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a cylinder
- **WHEN** `(make-cylinder radius height)` is called
- **THEN** it SHALL create an OC cylinder via `BRepPrimAPI_MakeCylinder`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a cone
- **WHEN** `(make-cone radius1 radius2 height)` is called
- **THEN** it SHALL create an OC cone via `BRepPrimAPI_MakeCone`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a circle
- **WHEN** `(make-circle radius)` is called
- **THEN** it SHALL create a 2D circle via OCCT edge/wire (or face) builders
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a polygon
- **WHEN** `(make-polygon points)` is called with at least 3 points
- **THEN** it SHALL create a closed 2D polygon via `BRepBuilderAPI_MakePolygon`
- **THEN** it SHALL return the OC shape handle


