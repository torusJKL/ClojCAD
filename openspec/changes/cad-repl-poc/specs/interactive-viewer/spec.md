## ADDED Requirements

### Requirement: Viewer displays 3D scene in the browser
The system SHALL display a Three.js viewport in the browser with orbit controls (rotate, pan, zoom).

#### Scenario: Viewport renders a mesh
- **WHEN** a mesh is pushed to the scene manager
- **THEN** the Three.js viewport SHALL render it in the 3D scene
- **THEN** the user SHALL be able to orbit, pan, and zoom via mouse/touch

### Requirement: Scene manager coordinates model display
The system SHALL provide a scene manager that maintains a map of displayed models and their meshes.

#### Scenario: Show a model
- **WHEN** user calls `(show model)` where model is a defmodel instance
- **THEN** the scene manager SHALL call the model with current params
- **THEN** the resulting mesh SHALL appear in the viewer

#### Scenario: Show is idempotent
- **WHEN** user calls `(show model)` twice with the same model name
- **THEN** only one layer entry SHALL exist in the scene manager
- **THEN** the second call SHALL update the existing entry

#### Scenario: Show with custom params
- **WHEN** user calls `(show model {:r 20})`
- **THEN** the model SHALL be evaluated with the provided params merged over the shared atom defaults

#### Scenario: Show with display options
- **WHEN** user calls `(show model {:r 10} {:opacity 0.5})`
- **THEN** the model SHALL be displayed at 50% opacity

### Requirement: Layer panel displays model hierarchy
The system SHALL display a layer panel (built with Reagent) listing all active models with visibility toggles.

#### Scenario: Layer panel lists models
- **WHEN** models are displayed via `show`
- **THEN** each model SHALL appear as a named entry in the layer panel
- **THEN** each model entry SHALL have a visibility checkbox

#### Scenario: Toggle model visibility
- **WHEN** user unchecks a model in the layer panel
- **THEN** that model's mesh SHALL be hidden in the viewport

#### Scenario: Intermediate shapes appear as sub-layers
- **WHEN** a model uses `tag` to label intermediate geometry
- **THEN** each tagged intermediate SHALL appear as a collapsible sub-layer under its parent model
- **THEN** checked sub-layers SHALL display the intermediate mesh

### Requirement: Reactive re-evaluation on param change
The scene manager SHALL watch the shared params atom and re-evaluate dirty models.

#### Scenario: Param change triggers re-evaluation
- **WHEN** user evaluates `(swap! params assoc :r 20)`
- **THEN** the scene manager SHALL identify models that depend on `:r`
- **THEN** only those models SHALL be re-evaluated
- **THEN** the viewer SHALL update with the new meshes

#### Scenario: Unchanged models are not re-evaluated
- **WHEN** user changes param `:r` that only model A depends on
- **THEN** model B (no dependency on `:r`) SHALL NOT be re-evaluated
- **THEN** model B's mesh SHALL remain unchanged in the viewer
