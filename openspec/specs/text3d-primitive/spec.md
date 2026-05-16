## Requirements

### Requirement: Kernel provides 3D text primitive
The system SHALL provide a `text3d` function that generates 3D solid geometry from a string and font.

#### Scenario: Create 3D text with defaults
- **WHEN** `(text3d "Hello" 36)` is called
- **THEN** it SHALL use the bundled "Cousine" font
- **THEN** it SHALL generate 3D geometry representing the text
- **THEN** it SHALL return a `TopoDS_Solid` shape handle
- **THEN** the text SHALL stand upright (Z axis is up)

#### Scenario: Specify font and height
- **WHEN** `(text3d "Hello" 36 :font "Cousine-Bold" :height 0.2)` is called
- **THEN** it SHALL use "Cousine-Bold" for the glyph outlines
- **THEN** the extrusion height SHALL be `0.2 * 36 = 7.2` units

#### Scenario: Return nil for unknown font
- **WHEN** `(text3d "Hello" 36 :font "NonExistent")` is called
- **THEN** it SHALL return nil
- **THEN** it SHALL log a warning to the console

#### Scenario: Handles multi-contour glyphs
- **WHEN** `(text3d "O" 36)` is called
- **THEN** the letter "O" SHALL have a hole (inner contour)
- **THEN** the shape SHALL be a valid solid, not self-intersecting

#### Scenario: Handles multi-character strings
- **WHEN** `(text3d "ABC" 36)` is called
- **THEN** all three characters SHALL be present in the result
- **THEN** the characters SHALL be positioned side by side

### Requirement: Kernel supports multiple font weights and styles
The system SHALL support font variants through the font name lookup.

#### Scenario: Text with bold font
- **WHEN** `(text3d "Hello" 36 :font "Cousine-Bold")` is called
- **THEN** the bold variant SHALL be used
- **THEN** the returned shape SHALL differ visibly from the regular variant

#### Scenario: Text with italic font
- **WHEN** `(text3d "Hello" 36 :font "Cousine-Italic")` is called
- **THEN** the italic variant SHALL be used
