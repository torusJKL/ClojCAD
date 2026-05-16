## ADDED Requirements

### Requirement: Scene manager params atom tests verify watch behavior
The scene manager (`ClojCAD.scene.manager`) SHALL have tests verifying the params atom and its watch behavior, isolated from the DOM/viewer dependency.

#### Scenario: Initial state has empty params atom
- **WHEN** the scene manager is loaded
- **THEN** the `params` atom SHALL be initialized as an empty map `{}`

#### Scenario: Params atom can be reset
- **WHEN** `(reset! params {:r 10})` is called
- **THEN** `@params` SHALL equal `{:r 10}`

### Requirement: Scene manager show/hide/remove logic tests
The scene manager SHALL have tests for model display state management, using a mocked viewer or isolated logic paths.

#### Scenario: Hide model updates scene state
- **WHEN** `hide-model` is called with a model name that exists in the scene
- **THEN** the model's `:visible?` SHALL be set to `false` in the scene atom

#### Scenario: Show model restores visibility
- **WHEN** `show-model` is called with a hidden model
- **THEN** the model's `:visible?` SHALL be set to `true`

#### Scenario: Remove model dissociates from scene
- **WHEN** `remove-model` is called with a model name in the scene
- **THEN** the model entry SHALL be removed from the scene atom

### Requirement: Scene manager set-opacity updates scene state
The scene manager SHALL have tests verifying opacity updates.

#### Scenario: Set opacity updates scene atom
- **WHEN** `set-opacity` is called with a model name and a new opacity value
- **THEN** the model's `:opts :opacity` SHALL be updated in the scene atom
