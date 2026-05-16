## ADDED Requirements

### Requirement: Kernel lifecycle tests verify tracked object management
The lifecycle namespace (`ClojCAD.kernel.lifecycle`) SHALL have tests verifying its atom-based object tracking.

#### Scenario: Track adds object to tracked set
- **WHEN** `track` is called with an object
- **THEN** the object SHALL be present in the tracked atom
- **THEN** `track` SHALL return the object itself

#### Scenario: Destroy removes object and calls .delete
- **WHEN** `destroy` is called with a tracked object
- **THEN** `.delete` SHALL be called on the object
- **THEN** the object SHALL be removed from the tracked atom

#### Scenario: Destroy-all clears all tracked objects
- **WHEN** `destroy-all` is called
- **THEN** every tracked object SHALL have `.delete` called
- **THEN** the tracked atom SHALL be empty

### Requirement: Kernel primitive tests verify OCCT wrapper calls
The primitives namespace (`ClojCAD.kernel.primitives`) SHALL have tests that verify functions return non-nil OCCT shapes (requires initialized WASM kernel).

#### Scenario: make-sphere returns a shape
- **WHEN** `make-sphere` is called with a positive radius
- **THEN** it SHALL return a non-nil OCCT TopoDS_Shape

#### Scenario: make-box returns a shape
- **WHEN** `make-box` is called with positive dimensions
- **THEN** it SHALL return a non-nil OCCT TopoDS_Shape

#### Scenario: make-cylinder returns a shape
- **WHEN** `make-cylinder` is called with positive radius and height
- **THEN** it SHALL return a non-nil OCCT TopoDS_Shape

#### Scenario: make-cone returns a shape
- **WHEN** `make-cone` is called with positive radii and height
- **THEN** it SHALL return a non-nil OCCT TopoDS_Shape

### Requirement: Boolean operation tests verify CSG results
The booleans namespace (`ClojCAD.kernel.booleans`) SHALL have tests verifying CSG operations produce valid shapes.

#### Scenario: fuse two overlapping spheres
- **WHEN** `fuse` is called with two overlapping spheres
- **THEN** it SHALL return a non-nil shape

#### Scenario: cut produces difference
- **WHEN** `cut` is called with a box and an overlapping cylinder
- **THEN** it SHALL return a non-nil shape

#### Scenario: common produces intersection
- **WHEN** `common` is called with two overlapping shapes
- **THEN** it SHALL return a non-nil shape

#### Scenario: non-overlapping common returns nil
- **WHEN** `common` is called with two non-overlapping shapes
- **THEN** it SHALL return nil

#### Scenario: variadic chaining works
- **WHEN** `fuse` is called with three or more overlapping shapes
- **THEN** it SHALL return a non-nil shape

### Requirement: Tessellation tests verify mesh output
The mesh namespace (`ClojCAD.kernel.mesh`) SHALL have tests verifying tessellate produces correctly structured output.

#### Scenario: tessellate returns expected keys
- **WHEN** `tessellate` is called with a valid shape
- **THEN** it SHALL return a map with keys `:vertices`, `:normals`, `:indices`, `:edges`, `:obj-vertices`, `:face-types`, `:edge-types`, `:triangles-per-face`, `:segments-per-edge`

#### Scenario: tessellate returns typed arrays
- **WHEN** `tessellate` is called with a valid shape
- **THEN** `:vertices` SHALL be a Float32Array
- **THEN** `:normals` SHALL be a Float32Array
- **THEN** `:indices` SHALL be a Uint32Array

#### Scenario: tessellate with custom deviation
- **WHEN** `tessellate` is called with a shape and a custom maxDeviation
- **THEN** it SHALL use the provided deviation instead of the default 0.1
