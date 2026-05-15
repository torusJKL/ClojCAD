## ADDED Requirements

### Requirement: Scene manager displays imported shapes
The system SHALL support adding imported shapes to the scene via the scene manager.

#### Scenario: Imported shape is added to scene
- **WHEN** a shape is imported from an STL or STEP file
- **THEN** the import handler SHALL create a model entry in the scene atom using the filename as the model name
- **THEN** the shape SHALL be tessellated via `kernel/tessellate`
- **THEN** the mesh SHALL be pushed to the viewer via the existing scene manager display pipeline
- **THEN** the imported shape SHALL appear in the viewer and tree panel
