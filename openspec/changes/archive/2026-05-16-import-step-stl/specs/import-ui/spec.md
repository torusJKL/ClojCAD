## ADDED Requirements

### Requirement: Import button in viewer toolbar
The system SHALL provide an "Import" button in the three-cad-viewer toolbar that opens a file picker for STEP and STL files.

#### Scenario: Import button appears in toolbar
- **WHEN** the viewer initializes
- **THEN** an import button SHALL be added to the toolbar
- **THEN** the button SHALL use the same CSS classes as existing toolbar buttons (`tcv_btn`, `tcv_btn_highlight`, `tcv_round`)
- **THEN** the button SHALL display an upload icon (up arrow)

#### Scenario: Clicking import button opens file picker
- **WHEN** the user clicks the import button
- **THEN** a file picker SHALL open
- **THEN** the file picker SHALL accept `.stl`, `.step`, and `.stp` extensions

### Requirement: Imported shape appears in scene
The system SHALL add successfully imported shapes to the 3D scene for display.

#### Scenario: Imported shape is displayed
- **WHEN** an STL or STEP file is successfully imported
- **THEN** the imported shape SHALL appear in the 3D viewport
- **THEN** the imported shape SHALL appear in the tree panel with the filename as its label
- **THEN** the imported shape SHALL be tessellatable (renderable) alongside parametric models

#### Scenario: Import failure is communicated
- **WHEN** an STL or STEP file fails to import
- **THEN** the system SHALL log a warning to the console
- **THEN** a visible error notification SHALL appear in the viewport
- **THEN** the user SHALL see no change in the scene

#### Scenario: Unsupported file type is rejected
- **WHEN** the user selects a file that is not `.stl`, `.step`, or `.stp`
- **THEN** the system SHALL display a visible error notification
- **THEN** the file SHALL NOT be imported

### Requirement: Loading indicator during import
The system SHALL display a loading overlay while an import is in progress.

#### Scenario: Loading overlay shows during file processing
- **WHEN** the user selects a file to import
- **THEN** a loading overlay SHALL appear over the viewport
- **WHEN** the import completes (success or failure)
- **THEN** the loading overlay SHALL be removed
