## CHANGED Requirements

### Requirement: Kernel loads zalo opencascade.js WASM
The system SHALL initialize the `opencascade.js` WebAssembly module (zalo fork, `cascadestudio-v2` branch) before any geometry operations. Import via CLJS `:default` require.

#### Scenario: Kernel initialization
- **WHEN** the page loads
- **THEN** the system SHALL import `opencascade.js` via `(require ["opencascade.js" :default init-oc])`
- **THEN** the kernel SHALL call `init-oc` with a `locateFile` callback pointing WASM requests to the server root
- **THEN** the viewer SHALL show a loading state while WASM initializes
- **WHEN** the promise resolves
- **THEN** the `oc-instance` atom SHALL hold the initialized OCCT module
- **THEN** the kernel SHALL be ready for geometry operations

#### Scenario: Geometry operations fail before init
- **WHEN** a model evaluation is attempted before WASM init completes
- **THEN** the system SHALL return an error (the affected model shows error state in layer panel)

### Requirement: Kernel provides real OCCT B-Rep primitive creation
The system SHALL wrap zalo fork OCCT primitive creation functions, returning `TopoDS_Shape` handles.

#### Scenario: Create a sphere
- **WHEN** the kernel function `(make-sphere radius)` is called
- **THEN** it SHALL create a `BRepPrimAPI_MakeSphere` with the given radius
- **THEN** it SHALL return the `.Shape()` as a `TopoDS_Shape` handle

#### Scenario: Create a box
- **WHEN** `(make-box width depth height)` is called
- **THEN** it SHALL create a `BRepPrimAPI_MakeBox` with the given dimensions
- **THEN** it SHALL return the `.Shape()` as a `TopoDS_Shape` handle

### Requirement: Kernel tessellates shapes to mesh data with normals and edges
The system SHALL convert OCCT `TopoDS_Shape` objects to mesh data (vertices, normals, triangles, edges) suitable for Three.js rendering.

#### Scenario: Tessellate a shape
- **WHEN** `(tessellate shape)` is called with a `TopoDS_Shape` handle
- **THEN** the kernel SHALL apply `BRepMesh_IncrementalMesh` with default `maxDeviation` of 0.1
- **THEN** the kernel SHALL iterate faces via `TopExp_Explorer(TopAbs_FACE)`
- **THEN** for each face, the kernel SHALL extract vertex positions via `Poly_Triangulation.get().Node(i)`
- **THEN** for each face, the kernel SHALL extract normals via `.Normal_1(i)`, respecting face orientation
- **THEN** for each face, the kernel SHALL extract triangle indices via `.Triangle(nt)`, correcting winding for reversed faces
- **THEN** the kernel SHALL concatenate per-face data into flat typed arrays
- **THEN** the kernel SHALL extract edges: iterate `TopAbs_EDGE`, sample points via `BRepAdaptor_Curve` + `GCPnts_TangentialDeflection`
- **THEN** the kernel SHALL return `{:vertices Float64Array :normals Float64Array :indices Uint32Array :edges Float64Array}`
- **THEN** all OCCT objects created during tessellation SHALL be freed via `.delete()`

#### Scenario: Edge extraction fallback
- **WHEN** `Poly_PolygonOnTriangulation` fails for an edge (or is not implemented)
- **THEN** the kernel SHALL fall back to `BRepAdaptor_Curve` + `GCPnts_TangentialDeflection` for edge point sampling

### Requirement: Kernel manages OC object lifecycle
The system SHALL track OCCT object allocations and provide cleanup.

#### Scenario: Explicit cleanup of OC objects
- **WHEN** a shape is tessellated
- **THEN** the shape handle SHALL be deleted after mesh data extraction
- **WHEN** a model is removed from the scene
- **THEN** any remaining OCCT handles for that model SHALL be freed

### Requirement: Kernel provides shape query operations
(Stretch) The system MAY wrap OCCT topology query functions for parameter inspection.

#### Scenario: Query sphere radius (optional)
- **WHEN** `(sphere-radius shape)` is called on an OC sphere `TopoDS_Shape`
- **THEN** it SHALL return the radius via OCCT topology traversal
