## Requirements
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
The scene manager SHALL watch the shared params atom and re-evaluate dirty models. (Unchanged from previous spec — the reactive watcher remains identical in logic.)

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

#### Scenario: Tags added via add-tags appear as sub-entries
- **WHEN** user calls `(add-tags 'my-model {:label shape})`
- **THEN** the new tag SHALL be added as a child part at path `"/my-model/:label"`
- **THEN** the new tag SHALL appear as a collapsible entry in the tree panel

#### Scenario: Tags removed via remove-tags disappear from tree
- **WHEN** user calls `(remove-tags 'my-model :label)` and the tag was previously shown in the tree
- **THEN** the child part at path `"/my-model/:label"` SHALL be removed
- **THEN** the tag entry SHALL disappear from the tree panel

### Requirement: Tagless models use flat structure; add-tags restructures to tree
`show` SHALL add tagless models as a flat part at `/root/<model-name>`. When `add-tags` is called on a tagless model, the scene manager SHALL restructure it by removing the flat part and adding a tree with a `<model-name>-body` child and the new tag children. Models already in tree form (with tags) receive new tags via child part updates.

#### Scenario: Tagless model displayed as flat part
- **WHEN** user calls `(show model)` and the model has no tags
- **THEN** the model SHALL be added as a flat part at `/root/<model-name>`

#### Scenario: First add-tags restructures flat model to tree
- **WHEN** user calls `(show model)` on a tagless model, then calls `(add-tags 'model {:label shape})`
- **THEN** the flat part at `/root/<model-name>` SHALL be removed
- **THEN** a tree part SHALL be added at `/root/<model-name>` containing a `<model-name>-body` child and a `:label` child
- **THEN** the `:label` tag SHALL appear in the scene atom's `:tags` map

#### Scenario: Adding tags to an already-tree model adds children
- **WHEN** a model already has tags in the scene and user calls `(add-tags 'model {:another shape})`
- **THEN** the new tag SHALL be added as a child part at `/root/<model-name>/:another`
- **THEN** existing children (including `<model-name>-body`) SHALL remain unchanged

### Requirement: Scene manager handles dynamic child part lifecycle
The scene manager SHALL support adding and removing individual child parts on an existing scene entry, including tessellation, viewer sync, and `:tags-visible` initialization.

#### Scenario: Added tag initializes as visible
- **WHEN** a tag is added via `add-tags`
- **THEN** the new tag SHALL be initialized with `:tags-visible` set to `true`

#### Scenario: Removed tag cleans up viewer and state
- **WHEN** a tag is removed via `remove-tags`
- **THEN** the scene atom's `:tags` and `:tags-visible` SHALL be dissoc'd for that label
- **THEN** the viewer SHALL remove the corresponding child part path

### Requirement: Scene manager displays imported shapes
The system SHALL support adding imported shapes to the scene via the scene manager.

#### Scenario: Imported shape is added to scene
- **WHEN** a shape is imported from an STL or STEP file
- **THEN** the import handler SHALL create a model entry in the scene atom using the filename as the model name
- **THEN** the shape SHALL be tessellated via `kernel/tessellate`
- **THEN** the mesh SHALL be pushed to the viewer via the existing scene manager display pipeline
- **THEN** the imported shape SHALL appear in the viewer and tree panel

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

