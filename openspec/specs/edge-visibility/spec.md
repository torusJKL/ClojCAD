# edge-visibility Specification

## Purpose
Edge rendering and visibility control for OCCT-tessellated shapes in three-cad-viewer.

## Requirements
### Requirement: Edge segments are rendered from OCCT tessellation

The system SHALL render edge lines on CAD objects, toggled by the three-cad-viewer toolbar's "Show black edges" button.

#### Scenario: Edges rendered on initial display

- **WHEN** `show` or `render-initial!` is called with a shape that has edge data from tessellation
- **THEN** the shape's `edges` field SHALL contain a `Float32Array` where every 6 consecutive float values define one line segment (2 × 3D points)
- **THEN** the three-cad-viewer SHALL render line segments for each edge of the CAD shape
- **THEN** edges SHALL be visible in the 3D viewport

#### Scenario: Edges update on param change

- **WHEN** a model parameter changes and the shape is re-tessellated
- **THEN** the updated edge data SHALL be passed via `updatePart`
- **THEN** the viewer SHALL re-render edges to match the new shape

#### Scenario: Black edges toggle changes edge color

- **WHEN** the user clicks the "Show black edges" toolbar button
- **THEN** the edge lines SHALL change to black color (`0x000000`)
- **WHEN** the user clicks the button again (deactivate)
- **THEN** the edge lines SHALL revert to the configured `edgeColor` (`0x707070`)

### Requirement: Edge data uses connected segment format

The OCCT edge tessellation SHALL produce output compatible with Three.js `LineSegmentsGeometry`.

#### Scenario: Curved edge produces connected segments

- **WHEN** an edge has N sample points (`p_1` through `p_N`) from `GCPnts_TangentialDeflection`
- **THEN** the output SHALL contain `(N-1) * 6` float values
- **THEN** the values SHALL represent connected segments: `[p1_xyz, p2_xyz, p2_xyz, p3_xyz, ..., p{N-1}_xyz, pN_xyz]`
- **THEN** each consecutive pair of sample points SHALL form one visible line segment without gaps

#### Scenario: Straight edge produces single segment

- **WHEN** an edge has exactly 2 sample points
- **THEN** the output SHALL contain exactly 6 float values
- **THEN** the single segment SHALL connect the two endpoints
