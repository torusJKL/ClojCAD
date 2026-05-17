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

### Requirement: Reactive re-evaluation on param change
The scene manager SHALL watch the shared params atom and re-evaluate dirty models.

#### Scenario: Param change triggers re-evaluation
- **WHEN** user evaluates `(swap! params assoc :r 20)`
- **THEN** the scene manager SHALL identify models that depend on `:r`
- **THEN** only those models SHALL be re-evaluated
- **THEN** the scene manager SHALL call `viewer.updatePart` with the new shape data

#### Scenario: Unchanged models are not re-evaluated
- **WHEN** user changes param `:r` that only model A depends on
- **THEN** model B (no dependency on `:r`) SHALL NOT be re-evaluated
- **THEN** model B's mesh SHALL remain unchanged in the viewer

### Requirement: Intermediate shapes appear as sub-layers
The system SHALL use the library's hierarchical tree to display tagged intermediate geometry as collapsible children of their parent model.

#### Scenario: Tagged shapes appear as sub-entries
- **WHEN** a model uses `tag` to label intermediate geometry
- **THEN** each tagged intermediate SHALL be added as a child part via `viewer.addPart("/<model-name>", childPart)`
- **THEN** each tagged intermediate SHALL appear as a collapsible entry under its model in the tree panel

#### Scenario: Tag visibility toggles
- **WHEN** user toggles a tagged intermediate in the library tree panel
- **THEN** the notification callback SHALL update the `scene` atom's `:tags-visible` map for that model and label

## REMOVED Requirements

### Requirement: Show tags on individual models
**Reason**: Subsumed by overloaded `show-model`/`hide-model`
**Migration**: Use `(show-model {:tag :sphere :model 'my-model})` instead of `(show-tag 'my-model :sphere)`

### Requirement: Hide tags on individual models
**Reason**: Subsumed by overloaded `show-model`/`hide-model`
**Migration**: Use `(hide-model {:tag :sphere :model 'my-model})` instead of `(hide-tag 'my-model :sphere)`

## ADDED Requirements

### Requirement: Unified show-model with filter map dispatch
`show-model` SHALL accept either a model identifier (symbol/keyword) for whole-model visibility, or a filter map for tag-level visibility operations.

#### Scenario: Symbol arg shows whole model (unchanged)
- **WHEN** user calls `(show-model 'my-model)`
- **THEN** the model SHALL be set to visible (`:visible?` = true)
- **THEN** the viewer SHALL update the model path visibility

#### Scenario: Map with :tag shows tagged sub-shapes on all matching models
- **WHEN** user calls `(show-model {:tag :sphere})` and models A and B both have a `:sphere` tag
- **THEN** `:tags-visible` SHALL be set to `true` for `:sphere` on both models
- **THEN** the viewer SHALL update visibility for both models' sphere paths

#### Scenario: Map with :tag and :model targets a specific model
- **WHEN** user calls `(show-model {:tag :sphere :model 'my-model})`
- **THEN** only `my-model`'s `:tags-visible` SHALL be updated for `:sphere`
- **THEN** other models' `:sphere` visibility SHALL NOT change

#### Scenario: Map with :tag and :name-matching restricts scope
- **WHEN** user calls `(show-model {:tag :sphere :name-matching "foo*"})`
- **THEN** only models whose name matches `"foo*"` SHALL have their `:sphere` tag shown

#### Scenario: Show with no matching models is a no-op
- **WHEN** user calls `(show-model {:tag :nonexistent})`
- **THEN** no state SHALL be modified

### Requirement: Unified hide-model with filter map dispatch
`hide-model` SHALL mirror `show-model`, accepting the same overloaded forms.

#### Scenario: Symbol arg hides whole model (unchanged)
- **WHEN** user calls `(hide-model 'my-model)`
- **THEN** the model SHALL be set to hidden (`:visible?` = false)
- **THEN** the viewer SHALL update the model path visibility

#### Scenario: Map with :tag hides tagged sub-shapes on all matching models
- **WHEN** user calls `(hide-model {:tag :sphere})` and models A and B both have a `:sphere` tag
- **THEN** `:tags-visible` SHALL be set to `false` for `:sphere` on both models
- **THEN** the viewer SHALL update visibility for both models' sphere paths

#### Scenario: Map with :tag and :model targets a specific model
- **WHEN** user calls `(hide-model {:tag :sphere :model 'my-model})`
- **THEN** only `my-model`'s `:tags-visible` SHALL be updated for `:sphere`

#### Scenario: Map with :tag and :name-matching restricts scope
- **WHEN** user calls `(hide-model {:tag :sphere :name-matching "foo*"})`
- **THEN** only models matching `"foo*"` SHALL have their `:sphere` tag hidden

### Requirement: Toggle visibility across models
The system SHALL provide a `toggle-model` function that inverts visibility, accepting the same filter map format as `show-model`/`hide-model` (`:tag`, `:model`, `:name-matching`).

#### Scenario: Toggle with :tag inverts tag visibility
- **WHEN** user calls `(toggle-model {:tag :sphere})` and model A has `:sphere` visible and model B has `:sphere` hidden
- **THEN** model A's `:sphere` SHALL become hidden and model B's `:sphere` SHALL become visible

#### Scenario: Toggle with :name-matching restricts scope
- **WHEN** user calls `(toggle-model {:tag :sphere :name-matching "foo*"})`
- **THEN** only models matching `"foo*"` SHALL have their `:sphere` toggled

#### Scenario: Toggle whole model visibility with :model
- **WHEN** user calls `(toggle-model {:model 'my-model})`
- **THEN** `my-model`'s `:visible?` SHALL be inverted

### Requirement: Bulk show/hide all models
The system SHALL provide functions to show or hide all models in the scene at once.

#### Scenario: Show all makes all models visible
- **WHEN** user calls `(show-all)`
- **THEN** every model in the scene SHALL have `:visible?` set to `true`
- **THEN** the viewer SHALL update visibility for all model paths

#### Scenario: Hide all makes all models hidden
- **WHEN** user calls `(hide-all)`
- **THEN** every model in the scene SHALL have `:visible?` set to `false`
- **THEN** the viewer SHALL update visibility for all model paths

#### Scenario: Show all with empty scene is a no-op
- **WHEN** user calls `(show-all)` with no models in the scene
- **THEN** no state SHALL be modified
