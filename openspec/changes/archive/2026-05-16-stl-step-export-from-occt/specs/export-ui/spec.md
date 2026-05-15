## ADDED Requirements

### Requirement: Export dropdown button appears in toolbar
The system SHALL display a dropdown "Export" button in the three-cad-viewer toolbar for triggering STL or STEP export of the currently displayed shape.

#### Scenario: Export button visible after viewer init
- **WHEN** the viewer finishes initialization
- **THEN** the toolbar SHALL contain an "Export" button with a dropdown arrow indicator
- **THEN** the "Export" button SHALL be styled consistently with other toolbar buttons

#### Scenario: Export dropdown shows both formats
- **WHEN** the user clicks the "Export" button
- **THEN** a dropdown menu SHALL appear with "Export STL (.stl)" and "Export STEP (.step)" options
- **WHEN** the user clicks outside the dropdown or selects an option
- **THEN** the dropdown SHALL close

### Requirement: Export dropdown triggers export of the displayed shape
The system SHALL call the kernel export function corresponding to the selected format option.

#### Scenario: STL export from dropdown
- **WHEN** the user clicks "Export STL (.stl)" in the dropdown
- **THEN** the system SHALL call the kernel export function with the currently displayed shape
- **THEN** the browser SHALL download an STL file named after the displayed model (e.g., `my-model.stl`)

#### Scenario: STEP export from dropdown
- **WHEN** the user clicks "Export STEP (.step)" in the dropdown
- **THEN** the system SHALL call the kernel export function with the currently displayed shape
- **THEN** the browser SHALL download a STEP file named after the displayed model (e.g., `my-model.step`)

#### Scenario: No shape loaded
- **WHEN** the user clicks an export option and no shape is currently displayed
- **THEN** the system SHALL NOT attempt to export
- **THEN** the system SHALL log a warning
- **THEN** a visible error notification SHALL appear in the viewport

#### Scenario: No shape data for current model
- **WHEN** the user clicks an export option and the current model has no OCCT shape data
- **THEN** the system SHALL NOT attempt to export
- **THEN** a visible error notification SHALL appear in the viewport

### Requirement: Loading indicator during export
The system SHALL display a loading overlay while an export operation is in progress.

#### Scenario: Loading overlay shows during export processing
- **WHEN** the user clicks an export option
- **THEN** a loading overlay SHALL appear over the viewport
- **WHEN** the export completes or fails
- **THEN** the loading overlay SHALL be removed

### Requirement: Export button is mounted after viewer initialization
The system SHALL add the Export dropdown button to the toolbar after the three-cad-viewer Display is constructed.

#### Scenario: Button appended to toolbar DOM
- **WHEN** the viewer initializes
- **THEN** the export button element SHALL be appended to the toolbar container (`display.cadTool.container`)
- **THEN** the export button SHALL use CSS classes matching the toolbar's visual style
