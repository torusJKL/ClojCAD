## Requirements

### Requirement: Box can be centered at origin
The system SHALL support an optional `centered?` parameter on `make-box` to center the box at the origin.

#### Scenario: Box centered at origin
- **WHEN** `(make-box 10 20 30 true)` is called
- **THEN** the box SHALL be positioned such that its center is at `(0, 0, 0)`
- **THEN** the box SHALL extend from `(-5, -10, -15)` to `(5, 10, 15)`

#### Scenario: Box not centered (default behavior preserved)
- **WHEN** `(make-box 10 20 30)` or `(make-box 10 20 30 false)` is called
- **THEN** the box SHALL extend from `(0, 0, 0)` to `(10, 20, 30)` (existing behavior)

### Requirement: Cylinder can be centered at origin
The system SHALL support an optional `centered?` parameter on `make-cylinder` to center the cylinder at the origin along its Z axis.

#### Scenario: Cylinder centered at origin
- **WHEN** `(make-cylinder 5 20 true)` is called
- **THEN** the cylinder SHALL be positioned such that its center is at `(0, 0, 0)`
- **THEN** the cylinder SHALL extend from `z = -10` to `z = 10`

#### Scenario: Cylinder not centered (default behavior preserved)
- **WHEN** `(make-cylinder 5 20)` or `(make-cylinder 5 20 false)` is called
- **THEN** the cylinder SHALL start at `z = 0` and extend to `z = 20` (existing behavior)
