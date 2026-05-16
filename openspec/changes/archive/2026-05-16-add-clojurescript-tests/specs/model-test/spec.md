## ADDED Requirements

### Requirement: Model registry tests verify registration and lookup
The registry namespace (`ClojCAD.model.registry`) SHALL have tests verifying model registration and lookup.

#### Scenario: Register and lookup a model
- **WHEN** a model entry is registered via `register!`
- **THEN** `lookup` with the same name SHALL return the entry
- **THEN** `registered-keys` SHALL include the registered name

#### Scenario: Lookup returns nil for unknown model
- **WHEN** `lookup` is called with an unregistered name
- **THEN** it SHALL return nil

### Requirement: Model tag tests verify dynamic binding behavior
The tag namespace (`ClojCAD.model.tag`) SHALL have tests verifying dynamic binding context.

#### Scenario: Tag records shape in context
- **WHEN** `tag` is called with a label and shape inside a `binding` of `*scene-context*`
- **THEN** the shape SHALL be recorded in the context atom under the given label
- **THEN** `tag` SHALL return the shape unchanged

#### Scenario: Tag is no-op outside context
- **WHEN** `tag` is called without `*scene-context*` being bound
- **THEN** `tag` SHALL return the shape unchanged
- **THEN** no side effects SHALL occur

### Requirement: Reactive model tests verify caching behavior
The model core namespace (`ClojCAD.model.core`) SHALL have tests verifying reactive model caching.

#### Scenario: Model returns cached result for same params
- **WHEN** a reactive model is called twice with the same params
- **THEN** the second call SHALL return the cached result (identical? reference)

#### Scenario: Model recomputes on different params
- **WHEN** a reactive model is called with different params than the previous call
- **THEN** the model SHALL recompute and return a new result
