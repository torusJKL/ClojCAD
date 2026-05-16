## Requirements

### Requirement: System provides font registry
The system SHALL maintain a global font registry mapping font names to parsed opentype.js Font objects.

#### Scenario: Bundled fonts available at init
- **WHEN** the kernel initializes
- **THEN** "Cousine", "Cousine-Bold", "Cousine-Italic", and "Cousine-BoldItalic" SHALL be registered
- **THEN** font loading SHALL complete after OCCT WASM initialization

#### Scenario: Register font from HTTP URL
- **WHEN** `(register-font! "MyFont" "https://example.com/font.ttf")` is called
- **THEN** the system SHALL fetch the TTF from the URL
- **THEN** it SHALL parse it via opentype.js
- **THEN** it SHALL register "MyFont" in the registry
- **THEN** the font SHALL be usable in subsequent `text3d` calls

#### Scenario: Registered fonts persist across page loads
- **WHEN** a font is registered via `register-font!`
- **THEN** its TTF data SHALL be stored in IndexedDB
- **WHEN** the page is reloaded
- **THEN** the font SHALL be restored from IndexedDB without re-fetching

#### Scenario: Double registration updates in-memory registry
- **WHEN** `(register-font! "MyFont" ...)` is called twice
- **THEN** the registry SHALL contain only the latest version
- **THEN** IndexedDB SHALL be updated with the new data

### Requirement: System lists registered fonts
The system SHALL provide a function to list all currently registered font names.

#### Scenario: List fonts
- **WHEN** `(list-fonts)` is called
- **THEN** it SHALL return a sequence of font name strings
- **THEN** bundled fonts ("Cousine", "Cousine-Bold", etc.) SHALL be included
- **THEN** custom fonts registered via `register-font!` SHALL be included

### Requirement: System provides font metadata
The system SHALL provide a function to inspect a registered font's metadata.

#### Scenario: Query font info
- **WHEN** `(font-info "Cousine")` is called
- **THEN** it SHALL return a map with at least `:name` and `:source` keys
- **THEN** `:source` SHALL be `:bundled` for bundled fonts
- **THEN** `:source` SHALL be `:custom` for registered fonts
- **THEN** opentype.js font metadata SHALL be included (e.g., `:family`, `:style`, `:glyphs` count)

#### Scenario: Query unknown font
- **WHEN** `(font-info "NonExistent")` is called
- **THEN** it SHALL return nil

### Requirement: System provides synchronous font loading
The system SHALL provide a synchronous `load-font!` function that blocks until the font is loaded and parsed, for use when the font must be available immediately.

#### Scenario: Load font synchronously
- **WHEN** `(load-font! "MyFont" "/fonts/MyFont.ttf")` is called
- **THEN** the system SHALL fetch the TTF using synchronous XHR
- **THEN** it SHALL parse it via opentype.js
- **THEN** it SHALL register "MyFont" in the registry
- **THEN** it SHALL return the font object (or nil on failure)
- **THEN** after the call returns, the font SHALL be usable in subsequent `text3d` calls without any async gap

#### Scenario: load-font! vs register-font! distinction
- **WHEN** `load-font!` is used, no promise is returned — the operation is complete when the call returns
- **WHEN** `register-font!` is used, a promise is returned and `text3d` may return nil if called before resolution
- **WHEN** deterministic font availability is required, `load-font!` SHALL be preferred over `register-font!`

### Requirement: IndexedDB persistence is best-effort
The system SHALL handle IndexedDB failures gracefully without blocking font usage in the current session.

#### Scenario: IndexedDB unavailable
- **WHEN** IndexedDB is unavailable (private browsing, quota exceeded)
- **THEN** the font SHALL still be available in the in-memory registry for the current session
- **THEN** a warning SHALL be logged to the console
- **THEN** the system SHALL NOT throw or crash
