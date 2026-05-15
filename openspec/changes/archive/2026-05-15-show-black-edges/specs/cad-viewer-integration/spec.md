# cad-viewer-integration Specification (Delta)

## MODIFIED Requirements

### Requirement: Shape data adapter converts tessellation to library format

The system SHALL convert the kernel's tessellation output to the `three-cad-viewer` `Shape` format for use with the Viewer API.

#### Scenario: Full Shape conversion

- **WHEN** tessellation produces `{:vertices :normals :indices :edges :obj-vertices :face-types :edge-types :triangles-per-face :segments-per-edge}`
- **THEN** the adapter SHALL rename `:indices` to `:triangles`
- **THEN** the adapter SHALL produce a `Shape` object with all 9 fields matching the library's schema
- **THEN** the adapter SHALL convert CLJS keyword keys to JS string keys
- **THEN** the `edges` field SHALL be a `Float32Array` with connected line segment format (every 6 floats = one segment)
