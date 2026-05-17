## Requirements

### Requirement: List all objects with optional filtering
The system SHALL provide a single `list-objects` function that returns all objects when called with no arguments, and accepts an optional filter map with `:tag`, `:visibility`, and/or `:name-matching` keys to narrow results.

#### Scenario: List all objects returns scene contents
- **WHEN** user calls `(list-objects)`
- **THEN** the function SHALL return a map of model-name → entry for every model in the scene atom
- **THEN** each entry SHALL include `:visible?`, `:tags`, `:tags-visible`, and `:opts` keys

#### Scenario: List returns empty map when scene is empty
- **WHEN** user calls `(list-objects)` with no models in the scene
- **THEN** the function SHALL return an empty map `{}`

#### Scenario: Filter by tag returns matching models
- **WHEN** user calls `(list-objects {:tag :sphere})` and models A and B have a `:sphere` tag
- **THEN** the function SHALL return only models A and B with their full entries

#### Scenario: Filter by non-existent tag returns empty
- **WHEN** user calls `(list-objects {:tag :nonexistent})`
- **THEN** the function SHALL return an empty map `{}`

#### Scenario: Filter by visibility returns visible models
- **WHEN** user calls `(list-objects {:visibility :visible})`
- **THEN** the function SHALL return only models where `:visible?` is `true`

#### Scenario: Filter by visibility returns hidden models
- **WHEN** user calls `(list-objects {:visibility :hidden})`
- **THEN** the function SHALL return only models where `:visible?` is `false`

#### Scenario: Combined filters narrow results
- **WHEN** user calls `(list-objects {:tag :sphere :visibility :visible})`
- **THEN** the function SHALL return only models that have a `:sphere` tag AND are visible

#### Scenario: Filter by name-matching returns matching models
- **WHEN** user calls `(list-objects {:name-matching "foo*"})` and models named `"foobar"` and `"foo"` exist
- **THEN** the function SHALL return only models whose name matches the glob pattern `"foo*"`

#### Scenario: Name-matching with regex object
- **WHEN** user calls `(list-objects {:name-matching #"^test-\d+"})` and models `"test-1"` and `"other"` exist
- **THEN** the function SHALL return only `"test-1"`

#### Scenario: Combined tag, visibility, and name-matching filters
- **WHEN** user calls `(list-objects {:tag :sphere :visibility :visible :name-matching "foo*"})`
- **THEN** the function SHALL return only models matching all three criteria

#### Scenario: Filter map with unknown keys ignores them
- **WHEN** user calls `(list-objects {:tag :sphere :unknown-key :val})`
- **THEN** the function SHALL filter by known keys (`:tag`, `:visibility`, `:name-matching`) and ignore unknown keys

### Requirement: Enumerate unique tag labels
The system SHALL provide a `list-tags` function that returns all unique tag labels across all models in the scene.

#### Scenario: List tags returns all unique labels
- **WHEN** user calls `(list-tags)` and models have tags `:sphere`, `:box`, and `:sphere`
- **THEN** the function SHALL return `#{:sphere :box}` (set of unique keywords)

#### Scenario: List tags returns empty set when no tags
- **WHEN** user calls `(list-tags)` and no models have tags
- **THEN** the function SHALL return an empty set `#{}`
