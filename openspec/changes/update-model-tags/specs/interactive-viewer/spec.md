## MODIFIED Requirements

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

## ADDED Requirements

### Requirement: All models use tree structure for viewer hierarchy
`show` SHALL always wrap model parts in a tree structure via `build-shapes-tree`, even when the model has no tags. This ensures the viewer hierarchy can accept child parts for runtime `add-tags`.

#### Scenario: Tagless model displayed as tree with single child
- **WHEN** user calls `(show model)` and the model has no tags
- **THEN** the model SHALL be added as a tree part at `/root/<model-name>`
- **THEN** the tree SHALL contain a single child part at `/root/<model-name>/<model-name>-body`

#### Scenario: Tagless tree structure supports add-tags
- **WHEN** user calls `(show model)` on a tagless model, then calls `(add-tags 'model {:label shape})`
- **THEN** the new tag SHALL appear as a child at `/root/<model-name>/:label`
- **THEN** the existing body child at `/root/<model-name>/<model-name>-body` SHALL remain unchanged

### Requirement: Scene manager handles dynamic child part lifecycle
The scene manager SHALL support adding and removing individual child parts on an existing scene entry, including tessellation, viewer sync, and `:tags-visible` initialization.

#### Scenario: Added tag initializes as visible
- **WHEN** a tag is added via `add-tags`
- **THEN** the new tag SHALL be initialized with `:tags-visible` set to `true`

#### Scenario: Removed tag cleans up viewer and state
- **WHEN** a tag is removed via `remove-tags`
- **THEN** the scene atom's `:tags` and `:tags-visible` SHALL be dissoc'd for that label
- **THEN** the viewer SHALL remove the corresponding child part path
