## ADDED Requirements

### Requirement: Default shape color is configurable at runtime

The system SHALL provide a configurable default shape color used when no `:color` is supplied in the opts map. The initial default SHALL be `0xfbd92c`.

#### Scenario: Default color used when opts has no :color key
- **WHEN** `build-part` is called without a `:color` key in opts
- **THEN** the resulting leaf SHALL use the current default shape color value

#### Scenario: Default color overridden by opts :color
- **WHEN** `build-part` is called with a `:color` key in opts
- **THEN** the opts `:color` value SHALL take precedence over the default

#### Scenario: Initial default is 0xfbd92c
- **WHEN** the application starts without a config.edn file
- **THEN** the default shape color SHALL be `0xfbd92c`

### Requirement: REPL function to change default color

The system SHALL expose a `set-default-shape-color!` function that accepts a hex integer and updates the default shape color atom.

#### Scenario: set-default-shape-color! updates the atom
- **WHEN** `set-default-shape-color!` is called with `0xff0000`
- **THEN** subsequent `build-part` calls without `:color` SHALL use `0xff0000`

### Requirement: Browser console function to change default color

The system SHALL expose a `setShapeColor` function on `js/window` that accepts a hex integer or CSS hex string and updates the default shape color atom.

#### Scenario: setShapeColor with hex integer
- **WHEN** `setShapeColor(0x00ff00)` is called from the browser console
- **THEN** the default shape color SHALL change to `0x00ff00`

#### Scenario: setShapeColor with CSS hex string
- **WHEN** `setShapeColor("#00ff00")` is called from the browser console
- **THEN** the default shape color SHALL change to `0x00ff00`

### Requirement: Persistent configuration via EDN file

The system SHALL load a `config.edn` file at startup and read the `:default-shape-color` key to initialize the default color.

#### Scenario: config.edn provides default color
- **WHEN** `config.edn` exists with `{:default-shape-color 0xff6600}`
- **THEN** the default shape color SHALL be `0xff6600` after startup

#### Scenario: config.edn missing falls back to default
- **WHEN** `config.edn` does not exist
- **THEN** the default shape color SHALL be `0xfbd92c`

#### Scenario: config.edn lacks :default-shape-color key
- **WHEN** `config.edn` exists but has no `:default-shape-color` key
- **THEN** the default shape color SHALL be `0xfbd92c`
