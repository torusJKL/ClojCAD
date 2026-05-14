## REMOVED Requirements

### Requirement: Viewer displays 3D scene in the browser
**Reason**: The viewport is now managed by the `three-cad-viewer` library's `Viewer` + `Display`. Manual Three.js scene/camera/renderer creation is no longer used. See `cad-viewer-integration` spec for the replacement.
**Migration**: Replace `init-viewport!` with `Display` + `Viewer` construction. Scene, camera, and renderer are created internally by the library.

### Requirement: Layer panel displays model hierarchy
**Reason**: The library provides a built-in tree panel. The Reagent layer panel is no longer needed.
**Migration**: The library's tree panel automatically shows models added via `addPart`. Remove `ui/layer_panel.cljs`. Tagged sub-geometry appears as hierarchical children.

## MODIFIED Requirements

### Requirement: Scene manager coordinates model display
The system SHALL provide a scene manager that maintains a map of displayed models and their meshes, using the three-cad-viewer Viewer API for rendering.

#### Scenario: Show a model
- **WHEN** user calls `(show model)` where model is a defmodel instance
- **THEN** the scene manager SHALL call the model with current params
- **THEN** the resulting mesh SHALL be converted to the library's Shape format
- **THEN** the scene manager SHALL call `viewer.addPart("/<model-name>", partData)`
- **THEN** the model SHALL appear in the 3D view and the library tree panel

#### Scenario: Show is idempotent
- **WHEN** user calls `(show model)` twice with the same model name
- **THEN** only one layer entry SHALL exist in the scene manager's `scene` atom
- **THEN** the second call SHALL call `viewer.updatePart` rather than adding a duplicate

#### Scenario: Show with custom params
- **WHEN** user calls `(show model {:r 20})`
- **THEN** the model SHALL be evaluated with the provided params merged over the shared atom defaults

#### Scenario: Show with display options
- **WHEN** user calls `(show model {:r 10} {:opacity 0.5})`
- **THEN** the `:opacity` option SHALL set the part's `alpha` field in the library Shape
- **THEN** the `:color` option SHALL set the part's `color` field (if provided)

### Requirement: Intermediate shapes appear as tree children
The system SHALL use the library's hierarchical tree to display tagged intermediate geometry as collapsible children of their parent model.

#### Scenario: Tagged shapes appear as sub-entries
- **WHEN** a model uses `tag` to label intermediate geometry
- **THEN** each tagged intermediate SHALL be added as a child part via `viewer.addPart("/<model-name>", childPart)`
- **THEN** each tagged intermediate SHALL appear as a collapsible entry under its model in the tree panel

#### Scenario: Tag visibility toggles
- **WHEN** user toggles a tagged intermediate in the library tree panel
- **THEN** the notification callback SHALL update the `scene` atom's `:tags-visible` map for that model and label

### Requirement: Reactive re-evaluation on param change
(Unchanged from previous spec — the reactive watcher remains identical in logic.)

#### Scenario: Param change triggers re-evaluation
- **WHEN** user evaluates `(swap! params assoc :r 20)`
- **THEN** the scene manager SHALL identify models that depend on `:r`
- **THEN** only those models SHALL be re-evaluated
- **THEN** the scene manager SHALL call `viewer.updatePart` with the new shape data

#### Scenario: Unchanged models are not re-evaluated
- **WHEN** user changes param `:r` that only model A depends on
- **THEN** model B (no dependency on `:r`) SHALL NOT be re-evaluated
- **THEN** model B's mesh SHALL remain unchanged in the viewer
