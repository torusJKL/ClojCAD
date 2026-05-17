## Requirements

### Requirement: Add new tagged sub-shapes to a model
The system SHALL provide an `add-tags` function that accepts either a model identifier (keyword or symbol) or a filter map (`:tag`, `:model`, `:name-matching`) as the first argument, followed by a map of tag-keyword → OCCT-shape. When given a filter map, the tags SHALL be added to all matching models.

#### Scenario: Add a single tag by model name
- **WHEN** user calls `(add-tags 'my-model {:label (kernel/make-sphere 5)})` and `'my-model` exists in the scene
- **THEN** the sphere SHALL be tessellated via `kernel/tessellate`
- **THEN** the scene atom's `:tags` for `'my-model` SHALL contain a `:label` entry with the tessellated mesh
- **THEN** the viewer SHALL add a child part at `"/my-model/:label"` with the sphere's shape data

#### Scenario: Add multiple tags atomically
- **WHEN** user calls `(add-tags 'my-model {:a (make-sphere 1) :b (make-box 2 2 2)})`
- **THEN** both `:a` and `:b` SHALL be tessellated
- **THEN** both SHALL appear as child parts under `'my-model`
- **THEN** both SHALL have their `:tags-visible` set to `true` by default

#### Scenario: Add tag with existing label replaces it
- **WHEN** user calls `(add-tags 'my-model {:sphere new-shape})` and a `:sphere` tag already exists on `'my-model`
- **THEN** the existing `:sphere` entry SHALL be replaced with the new shape's tessellated mesh
- **THEN** the viewer SHALL call `updatePart` on the `"/my-model/:sphere"` path

#### Scenario: Add tag to non-existent model is a no-op
- **WHEN** user calls `(add-tags 'nonexistent {:label (make-sphere 1)})`
- **THEN** no change SHALL be made to the scene atom
- **THEN** no viewer call SHALL be made

#### Scenario: Added tags inherit model display options
- **WHEN** the model has `{:color 0xff0000 :opacity 0.5}` display options
- **THEN** new tag shapes SHALL inherit those display options from the parent model entry

#### Scenario: Add tags with filter map targets matching models
- **WHEN** user calls `(add-tags {:name-matching "temp*"} {:label (make-sphere 1)})` and models `"temp-a"` and `"temp-b"` exist
- **THEN** both `"temp-a"` and `"temp-b"` SHALL receive a `:label` tag with the tessellated sphere
- **THEN** the viewer SHALL add child parts for both models

#### Scenario: Add tags with filter map and no matches is a no-op
- **WHEN** user calls `(add-tags {:tag :sphere} {:cone (make-cone 5 2 10)})` and no model has a `:sphere` tag
- **THEN** no change SHALL be made to the scene atom

### Requirement: Remove tagged sub-shapes from a model
The system SHALL provide a `remove-tags` function that accepts either a model identifier (keyword or symbol) or a filter map (`:tag`, `:model`, `:name-matching`) as the first argument, followed by one or more tag keywords to remove.

#### Scenario: Remove a single tag by model name
- **WHEN** user calls `(remove-tags 'my-model :sphere)`
- **THEN** the `:sphere` entry SHALL be dissoc'd from the scene atom's `:tags` map for `'my-model`
- **THEN** the `:sphere` entry SHALL be dissoc'd from `:tags-visible` for `'my-model`
- **THEN** the viewer SHALL remove the child part at `"/my-model/:sphere"`

#### Scenario: Remove multiple tags
- **WHEN** user calls `(remove-tags 'my-model :sphere :box)`
- **THEN** both `:sphere` and `:box` SHALL be removed from the scene atom `:tags` and `:tags-visible`
- **THEN** viewer child parts for both SHALL be removed

#### Scenario: Remove non-existent tag is a no-op
- **WHEN** user calls `(remove-tags 'my-model :nonexistent)`
- **THEN** no change SHALL be made to the scene atom
- **THEN** no viewer call SHALL be made

#### Scenario: Remove from non-existent model is a no-op
- **WHEN** user calls `(remove-tags 'nonexistent :sphere)`
- **THEN** no change SHALL be made to the scene atom

#### Scenario: Remove tags with filter map targets matching models
- **WHEN** user calls `(remove-tags {:tag :sphere :name-matching "temp*"} :sphere)`
- **THEN** only models whose name matches `"temp*"` and have a `:sphere` tag SHALL have `:sphere` removed
- **THEN** the viewer SHALL remove child parts for affected models

#### Scenario: Remove tags with filter map and no matches is a no-op
- **WHEN** user calls `(remove-tags {:tag :nonexistent} :sphere)`
- **THEN** no change SHALL be made to the scene atom

### Requirement: Tag mutations reset on model re-evaluation
Tag mutations applied via `add-tags`/`remove-tags` SHALL be ephemeral — when a model is re-evaluated (e.g., due to param changes), its `:tags` map SHALL be recomputed from the model definition, and any previously mutated tags SHALL be lost.

#### Scenario: Param change resets mutated tags
- **WHEN** user calls `(add-tags 'my-model {:temp (make-sphere 5)})` then changes a parameter that triggers re-evaluation of `'my-model`
- **THEN** the `:temp` tag SHALL NOT be present after re-evaluation
- **THEN** only tags defined in the model body SHALL appear in the `:tags` map

#### Scenario: Re-evaluation with no tags resets to empty
- **WHEN** a model with no tags in its body has `(add-tags ...)` called, and then a param change triggers re-evaluation
- **THEN** the model SHALL have no tags after re-evaluation

### Requirement: Tag mutation functions return updated tag map
All tag mutation functions (`add-tags`, `remove-tags`) SHALL return the updated `:tags` map when given a model identifier, or a map of `model-name → tags-map` when given a filter map, reflecting the state after mutation.

#### Scenario: add-tags by model name returns single tags map
- **WHEN** user calls `(add-tags 'my-model {:a (make-sphere 1)})`
- **THEN** the return value SHALL be a map containing `:a` (and any pre-existing tags)

#### Scenario: remove-tags by model name returns remaining tags map
- **WHEN** user calls `(remove-tags 'my-model :a)` and the model had `{:a ... :b ...}`
- **THEN** the return value SHALL be `{:b ...}`

#### Scenario: add-tags with filter map returns map of results
- **WHEN** user calls `(add-tags {:name-matching "temp*"} {:a (make-sphere 1)})` and two models match
- **THEN** the return value SHALL be `{"temp-a" {:a ...} "temp-b" {:a ...}}`

#### Scenario: remove-tags with filter map returns map of results
- **WHEN** user calls `(remove-tags {:name-matching "temp*"} :a)` and one model had `:a` and one did not
- **THEN** the return value SHALL be `{"temp-a" {...remaining...}}` (only models that were actually modified)
