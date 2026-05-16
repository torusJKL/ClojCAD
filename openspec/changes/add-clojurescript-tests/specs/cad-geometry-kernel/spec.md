## ADDED Requirements

### Requirement: Each public kernel function has a corresponding test
Every public function in the kernel API SHALL be covered by at least one test case.

#### Scenario: All kernel API functions are tested
- **WHEN** the test suite runs
- **THEN** each function listed in `ClojCAD.kernel.api` SHALL have at least one `deftest` in the corresponding test namespace

### Requirement: Kernel functions handle invalid inputs gracefully
Kernel wrapper functions SHALL produce predictable results (nil or error) when given invalid inputs, rather than crashing.

#### Scenario: make-sphere with negative radius
- **WHEN** `make-sphere` is called with a negative radius
- **THEN** it SHALL either return nil or propagate the OCCT error without crashing the runtime
