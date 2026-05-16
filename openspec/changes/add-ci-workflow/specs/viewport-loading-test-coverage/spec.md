## ADDED Requirements

### Requirement: Loading module state management is tested
The system SHALL have tests covering the `ClojCAD.viewport.loading` namespace for loading overlay and toast notification behavior.

#### Scenario: show-loading! creates overlay element
- **WHEN** `show-loading!` is called
- **THEN** a loading overlay div is created and appended to the document body
- **THEN** `*overlay` atom contains the overlay element
- **THEN** a spinner child element is present in the overlay
- **THEN** inline styles are set for fixed positioning, z-index, flex centering, and semi-transparent background

#### Scenario: show-loading! is idempotent (no duplicate overlay)
- **WHEN** `show-loading!` is called twice
- **THEN** only one overlay element exists in the document body
- **THEN** `*overlay` atom still contains a single overlay element

#### Scenario: hide-loading! removes overlay
- **WHEN** `hide-loading!` is called after `show-loading!`
- **THEN** the overlay element is removed from the document body
- **THEN** `*overlay` atom is reset to nil

#### Scenario: hide-loading! is safe when no overlay exists
- **WHEN** `hide-loading!` is called without a prior `show-loading!`
- **THEN** no error is thrown
- **THEN** `*overlay` atom remains nil

#### Scenario: notify! creates a toast element
- **WHEN** `notify!` is called with a message string
- **THEN** a toast div is created and appended to the document body
- **THEN** the toast text content matches the input message
- **THEN** the toast has inline styles for bottom-center positioning, red background, white text

#### Scenario: toast is removed after timeout
- **WHEN** `notify!` is called
- **THEN** the toast element is removed from the document body after approximately 4000ms
