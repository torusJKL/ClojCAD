# cad-viewer-integration Specification

## Purpose
TBD - created by archiving change replace-threejs-with-cad-viewer. Update Purpose after archive.
## Requirements
### Requirement: Viewer initializes from three-cad-viewer library

The system SHALL initialize a `Viewer` instance from the `three-cad-viewer` library instead of manually creating a Three.js scene/camera/renderer.

#### Scenario: Viewer creation

- **WHEN** the application starts
- **THEN** the system SHALL create a `Display` instance in the viewport container element
- **THEN** the system SHALL create a `Viewer` instance linked to the `Display`
- **THEN** the `Viewer` SHALL render the 3D scene with built-in controls (orbit, pan, zoom, view presets)
- **THEN** the `Display` SHALL render a toolbar, tree panel, and canvas area

#### Scenario: Library CSS is loaded

- **WHEN** the page loads
- **THEN** the `three-cad-viewer.css` stylesheet SHALL be loaded in the document `<head>`
- **THEN** the viewport container SHALL be styled correctly by the library's CSS

### Requirement: Shape data adapter converts tessellation to library format

The system SHALL convert the kernel's tessellation output to the `three-cad-viewer` `Shape` format for use with the Viewer API.

#### Scenario: Full Shape conversion

- **WHEN** tessellation produces `{:vertices :normals :indices :edges :obj-vertices :face-types :edge-types :triangles-per-face :segments-per-edge}`
- **THEN** the adapter SHALL rename `:indices` to `:triangles`
- **THEN** the adapter SHALL produce a `Shape` object with all 9 fields matching the library's schema
- **THEN** the adapter SHALL convert CLJS keyword keys to JS string keys
- **THEN** the `edges` field SHALL be a `Float32Array` with connected line segment format (every 6 floats = one segment)

### Requirement: Scene manager calls Viewer API for rendering

The scene manager SHALL use `Viewer.addPart`, `Viewer.updatePart`, and `Viewer.removePart` as the rendering side-effect instead of manually building and adding Three.js meshes.

#### Scenario: Show calls addPart

- **WHEN** `(show model)` is called
- **THEN** the scene manager SHALL evaluate the model and tessellate the shape as before
- **THEN** the scene manager SHALL store the result in the `scene` atom (bookkeeping)
- **THEN** the scene manager SHALL call `viewer.addPart` with the model name as the parent path and the shape data
- **THEN** the model SHALL appear in the library's tree panel and 3D view

#### Scenario: Param change calls updatePart

- **WHEN** a param change triggers re-evaluation of a model
- **THEN** the scene manager SHALL re-evaluate and tessellate the model
- **THEN** the scene manager SHALL update the `scene` atom
- **THEN** the scene manager SHALL call `viewer.updatePart` with the new shape data
- **THEN** the viewer SHALL update the mesh without a full scene rebuild

#### Scenario: Hide calls removePart

- **WHEN** `(hide model-name)` is called
- **THEN** the scene manager SHALL update the `scene` atom visibility
- **THEN** the scene manager SHALL call `viewer.removePart` with the model's path
- **THEN** the model SHALL be removed from the 3D view and tree panel

### Requirement: Library tree panel syncs with scene atom

The system SHALL use the library's built-in navigation tree panel for model visibility management, replacing the Reagent layer panel.

#### Scenario: Library tree panel shows models

- **WHEN** models are added via `viewer.addPart`
- **THEN** each model SHALL appear as an entry in the library's tree panel
- **THEN** tagged sub-geometry SHALL appear as collapsible children under their parent model

#### Scenario: Tree panel state syncs to scene atom

- **WHEN** the user toggles visibility in the library tree panel
- **THEN** the library SHALL invoke the Viewer's notification callback
- **THEN** the notification callback SHALL parse the change payload
- **THEN** the callback SHALL update the `scene` atom's visibility state to match

