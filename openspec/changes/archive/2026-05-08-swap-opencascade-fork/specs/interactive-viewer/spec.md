## CHANGED Requirements

### Requirement: Viewer displays 3D scene in the browser
The system SHALL display a Three.js viewport in the browser with orbit controls (rotate, pan, zoom).
(Unchanged from previous spec.)

#### Scenario: Viewport renders a mesh with normals and edges
- **WHEN** a mesh map `{:vertices :normals :indices :edges}` is pushed to the scene manager
- **THEN** the Three.js viewport SHALL render the face mesh with per-vertex normals for smooth shading
- **THEN** the viewport SHALL render edge lines (non-indexed `LineSegments`) overlaid on the face mesh
- **THEN** the user SHALL be able to orbit, pan, and zoom via mouse/touch

### Requirement: Mesh builder creates Three.js objects from mesh data
(Modified to handle normals and edges.)

#### Scenario: Build mesh with normals
- **WHEN** `(build-mesh mesh-data)` is called with `{:vertices [...] :normals [...] :indices [...] :edges [...]}`
- **THEN** it SHALL create a `BufferGeometry` with both `position` and `normal` `BufferAttribute`s
- **THEN** it SHALL set the index buffer from `:indices`
- **THEN** it SHALL return `{:face-mesh (Mesh) :edge-mesh (LineSegments)}`

#### Scenario: Edge rendering
- **WHEN** `:edges` is a non-empty `Float64Array`
- **THEN** the mesh builder SHALL create a non-indexed `BufferGeometry` from the edge points
- **THEN** the edge geometry SHALL be rendered as `LineSegments` with a distinct color (e.g. dark gray `0x707070`)
- **WHEN** `:edges` is empty or nil
- **THEN** no edge mesh SHALL be created

### Requirement: Scene manager coordinates model display
(Unchanged from previous spec.)

### Requirement: Layer panel displays model hierarchy
(Unchanged from previous spec.)

### Requirement: Reactive re-evaluation on param change
(Unchanged from previous spec.)
