## Requirements

### Requirement: Kernel provides 2D polygon primitive
The system SHALL provide a function to create 2D polygons from a sequence of points, as either wire or face topology.

#### Scenario: Create a polygon as a wire
- **WHEN** `(make-polygon [[x1 y1] [x2 y2] ... [xn yn]] true)` is called with at least 3 points
- **THEN** it SHALL create a `TopoDS_Wire` representing the closed polygon outline
- **THEN** it SHALL return the wire shape handle

#### Scenario: Create a polygon as a face
- **WHEN** `(make-polygon [[x1 y1] [x2 y2] ... [xn yn]] false)` is called (or `(make-polygon points)`, defaulting to false)
- **THEN** it SHALL create a `TopoDS_Face` representing the polygonal planar face
- **THEN** it SHALL return the face shape handle

#### Scenario: Polygon accepts 3D points with z=0 default
- **WHEN** `(make-polygon [[x1 y1] [x2 y2] [x3 y3]])` is called with 2D points
- **THEN** the points SHALL be treated as `(x y 0)` in 3D space

#### Scenario: Polygon rejects insufficient points
- **WHEN** `(make-polygon [])` or `(make-polygon [[1 2] [3 4]])` is called with fewer than 3 points
- **THEN** the kernel SHALL return nil

#### Scenario: Polygon wire is closed
- **WHEN** `(make-polygon points true)` is called
- **THEN** the resulting wire SHALL be closed (last point connects back to first)
