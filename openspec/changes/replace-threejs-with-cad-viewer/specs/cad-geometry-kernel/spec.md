## MODIFIED Requirements

### Requirement: Kernel tessellates shapes to mesh data with full Shape metadata
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
