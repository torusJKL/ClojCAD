## ADDED Requirements

### Requirement: Kernel loads opencascade.js WASM
The system SHALL initialize the opencascade.js WebAssembly module before any geometry operations.

#### Scenario: Kernel initialization
- **WHEN** the page loads
- **THEN** the system SHALL begin loading the opencascade.js WASM binary
- **THEN** the viewer SHALL show a loading state
- **WHEN** WASM initialization completes
- **THEN** the kernel SHALL be ready for geometry operations

#### Scenario: Geometry operations fail before init
- **WHEN** a model evaluation is attempted before WASM init completes
- **THEN** the system SHALL queue the operation or return an error

### Requirement: Kernel provides primitive shape creation
The system SHALL wrap opencascade.js primitive creation functions.

#### Scenario: Create a sphere
- **WHEN** the kernel function `(make-sphere radius)` is called
- **THEN** it SHALL create an OC sphere with the given radius via `BRepPrimAPI_MakeSphere`
- **THEN** it SHALL return the OC shape handle

#### Scenario: Create a box
- **WHEN** the kernel function `(make-box width depth height)` is called
- **THEN** it SHALL create an OC box via `BRepPrimAPI_MakeBox`
- **THEN** it SHALL return the OC shape handle

### Requirement: Kernel tessellates shapes to mesh data
The system SHALL convert OC B-Rep shapes to mesh data (vertices + triangles) suitable for Three.js.

#### Scenario: Tessellate a shape
- **WHEN** a shape is tessellated
- **THEN** the kernel SHALL produce a map with `:vertices` (flat float array of xyz positions) and `:indices` (triangle vertex indices)
- **THEN** the mesh data SHALL be usable by Three.js `BufferGeometry`

### Requirement: Kernel manages OC object lifecycle
The system SHALL track OC object allocations and provide cleanup.

#### Scenario: Explicit cleanup of OC objects
- **WHEN** a shape is no longer displayed
- **THEN** the kernel SHALL call `delete()` on the OC shape handle to free WASM heap memory

### Requirement: Kernel provides shape query operations
The system SHALL wrap OC topology query functions.

#### Scenario: Query radius of a sphere
- **WHEN** `(sphere-radius shape)` is called on an OC sphere shape
- **THEN** it SHALL return the radius via OC topology traversal
