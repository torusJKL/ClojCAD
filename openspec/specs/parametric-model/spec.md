## Requirements

### Requirement: User can define parametric models via defmodel macro
The system SHALL provide a `defmodel` macro that defines a parametric shape model with named parameters and a body that computes the shape.

#### Scenario: Define a model with parameters
- **WHEN** user evaluates `(defmodel sphere [r] (make-sphere r))`
- **THEN** a callable model `sphere` is defined
- **THEN** calling `(sphere {:r 5})` computes and returns a shape

#### Scenario: Model captures parameter schema
- **WHEN** user defines `(defmodel cube [w d h] ...)`
- **THEN** the model SHALL expose its parameter keys `[:w :d :h]` for dependency tracking

### Requirement: Models support default parameter values
The system SHALL support Clojure destructuring defaults in model parameter definitions.

#### Scenario: Default parameter values
- **WHEN** user defines `(defmodel sphere [{:keys [r] :or {r 10}}] (make-sphere r))`
- **THEN** calling `(sphere {})` SHALL use the default radius of 10

### Requirement: Model parameters are tracked from a shared atom
Models SHALL dereference parameters from a shared `params` atom when computing.

#### Scenario: Parameter dereferencing
- **WHEN** `(def params (atom {:r 10}))` and model `sphere` uses `(:r @params)`
- **THEN** calling `(sphere)` SHALL return a sphere of radius 10

### Requirement: Display hint metadata
Models SHALL support optional `^{:opacity 0.3}` metadata for default display properties.

#### Scenario: Default opacity metadata
- **WHEN** user defines `(defmodel ^{:opacity 0.3} helper [r] (make-sphere r))`
- **THEN** calling `(helper {:r 5})` SHALL display the sphere at 30% opacity

### Requirement: Tag macro for intermediate geometry
The system SHALL provide a `tag` macro that marks intermediate shapes with a label for optional display.

#### Scenario: Tag an intermediate shape
- **WHEN** user writes `(tag :plate (box 10 20 30))`
- **THEN** `tag` SHALL return the shape unchanged
- **THEN** the scene SHALL record the shape under the label `:plate` in the current model's hierarchy
- **THEN** the layer panel SHALL show `:plate` as a toggleable sub-layer

#### Scenario: Tag has no effect outside a model context
- **WHEN** user writes `(tag :foo (sphere 5))` at the REPL without an active model
- **THEN** `tag` SHALL return the sphere unchanged
- **THEN** no intermediate SHALL be recorded
