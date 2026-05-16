## Requirements
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

### Requirement: Kernel tessellates shapes to mesh data
The system SHALL convert OCCT `TopoDS_Shape` objects to mesh data including per-face and per-edge metadata for the three-cad-viewer library.

#### Scenario: Tessellate a shape with full metadata
- **WHEN** `(tessellate shape)` is called with a `TopoDS_Shape` handle
- **THEN** the kernel SHALL apply `BRepMesh_IncrementalMesh` with default `maxDeviation` of 0.1
- **THEN** the kernel SHALL iterate faces via `TopExp_Explorer(TopAbs_FACE)`
- **THEN** for each face, the kernel SHALL extract vertex positions via `Poly_Triangulation.get().Node(i)`
- **THEN** for each face, the kernel SHALL extract normals via `.Normal_1(i)`, respecting face orientation
- **THEN** for each face, the kernel SHALL extract triangle indices via `.Triangle(nt)`, correcting winding for reversed faces
- **THEN** for each face, the kernel SHALL determine the face type via `BRepAdaptor_Surface.GetSurfaceType()` (Plane=0, Cylinder=1, Cone=2, Sphere=3, Torus=4, ...)
- **THEN** for each face, the kernel SHALL record the number of triangles in `:triangles-per-face`
- **THEN** for each face, the kernel SHALL record per-face unique vertices in `:obj-vertices`
- **THEN** the kernel SHALL concatenate per-face data into flat typed arrays
- **THEN** the kernel SHALL iterate edges via `TopExp_Explorer(TopAbs_EDGE)`
- **THEN** for each edge, the kernel SHALL determine the edge type via `BRepAdaptor_Curve.GetCurveType()` (Line=0, Circle=1, Ellipse=2, ...)
- **THEN** for each edge, the kernel SHALL sample points via `BRepAdaptor_Curve` + `GCPnts_TangentialDeflection`
- **THEN** for each edge, the kernel SHALL record the number of segments in `:segments-per-edge`
- **THEN** the kernel SHALL return `{:vertices Float64Array :normals Float64Array :indices Uint32Array :edges Float64Array :obj-vertices Float64Array :face-types (array of ints) :edge-types (array of ints) :triangles-per-face (array of ints) :segments-per-edge (array of ints)}`

#### Scenario: Edge extraction fallback
- **WHEN** `Poly_PolygonOnTriangulation` fails for an edge
- **THEN** the kernel SHALL fall back to `BRepAdaptor_Curve` + `GCPnts_TangentialDeflection` for edge point sampling

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

### Requirement: Kernel initializes export-related OCCT classes
The system SHALL load StlAPI_Writer and STEPControl_Writer classes as part of the OCCT WASM initialization. These classes are available in the rebuilt opencascade.js fork and do not require additional async loading.

#### Scenario: Export classes available after init
- **WHEN** the kernel finishes initialization
- **THEN** `oc.StlAPI_Writer` SHALL be a valid constructor
- **THEN** `oc.STEPControl_Writer` SHALL be a valid constructor
- **THEN** `oc.STEPControl_StepDataVersion` SHALL have `STEPControl_AsIs` defined

### Requirement: Export function result does not leak OCCT objects
The system SHALL properly clean up all OCCT objects allocated during export.

#### Scenario: Writer objects are deleted after use
- **WHEN** `(kernel/export-stl ...)` or `(kernel/export-step ...)` completes (success or failure)
- **THEN** the `StlAPI_Writer` or `STEPControl_Writer` instance SHALL be deleted via `.delete()`
- **THEN** any intermediate OCCT objects (strings, transfer processes) SHALL be deleted

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

