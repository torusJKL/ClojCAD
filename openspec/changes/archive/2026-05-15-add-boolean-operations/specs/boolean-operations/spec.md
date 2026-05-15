## ADDED Requirements

### Requirement: Kernel provides boolean union (fuse)
The system SHALL combine two OCCT shapes into a single shape via `BRepAlgoAPI_Fuse`.

#### Scenario: Fuse two shapes
- **WHEN** `(kernel/fuse shape-a shape-b)` is called with two valid `TopoDS_Shape` handles
- **THEN** it SHALL create a `BRepAlgoAPI_Fuse` builder with both shapes
- **THEN** it SHALL return a new `TopoDS_Shape` representing the union
- **THEN** it SHALL delete the builder after extracting the result
- **THEN** it SHALL register the result shape with the lifecycle tracker

#### Scenario: Fuse with non-overlapping shapes
- **WHEN** `(kernel/fuse shape-a shape-b)` is called with shapes that do not intersect
- **THEN** it SHALL return a shape containing both as separate shells (OCCT behavior: non-intersecting fuse returns compound)

### Requirement: Kernel provides boolean intersection (common)
The system SHALL compute the intersection volume of two OCCT shapes via `BRepAlgoAPI_Common`.

#### Scenario: Common intersection of two shapes
- **WHEN** `(kernel/common shape-a shape-b)` is called with two valid overlapping `TopoDS_Shape` handles
- **THEN** it SHALL create a `BRepAlgoAPI_Common` builder with both shapes
- **THEN** it SHALL return a new `TopoDS_Shape` representing the intersecting volume
- **THEN** it SHALL delete the builder after extracting the result
- **THEN** it SHALL register the result shape with the lifecycle tracker

#### Scenario: Common with non-intersecting shapes
- **WHEN** `(kernel/common shape-a shape-b)` is called with shapes that do not intersect
- **THEN** it SHALL return nil (no result)

### Requirement: Kernel provides boolean difference (cut)
The system SHALL subtract one OCCT shape from another via `BRepAlgoAPI_Cut`.

#### Scenario: Cut one shape from another
- **WHEN** `(kernel/cut shape-a shape-b)` is called with two valid `TopoDS_Shape` handles
- **THEN** it SHALL create a `BRepAlgoAPI_Cut` builder, subtracting shape-b from shape-a
- **THEN** it SHALL return a new `TopoDS_Shape` representing the difference
- **THEN** it SHALL delete the builder after extracting the result
- **THEN** it SHALL register the result shape with the lifecycle tracker

#### Scenario: Cut with non-overlapping shapes
- **WHEN** `(kernel/cut shape-a shape-b)` is called where shape-b does not intersect shape-a
- **THEN** it SHALL return a copy of shape-a (OCCT behavior: cut with no intersection returns original)

### Requirement: Boolean operations handle failure gracefully
The system SHALL handle null/invalid OCCT results without crashing.

#### Scenario: Null shape from failed operation
- **WHEN** a boolean operation builder returns null or an IsNull shape
- **THEN** the kernel SHALL return nil
- **THEN** the kernel SHALL NOT attempt to track or use the null shape

#### Scenario: Builder throws exception
- **WHEN** a boolean operation builder throws during construction or .Shape() call
- **THEN** the kernel SHALL catch the exception
- **THEN** the kernel SHALL log a warning via `js/console.warn`
- **THEN** the kernel SHALL return nil

### Requirement: Boolean results are tessellatable
The system SHALL support tessellation of boolean result shapes identically to primitive shapes.

#### Scenario: Tessellate fused shape
- **WHEN** a shape produced by `kernel/fuse` is passed to `kernel/tessellate`
- **THEN** it SHALL produce the same mesh format as primitive shapes: `{:vertices :normals :indices :edges ...}`

### Requirement: Boolean operations accept variadic arguments
The system SHALL support more than two input shapes via sequential chaining.

#### Scenario: Fuse three shapes
- **WHEN** `(kernel/fuse a b c)` is called with three valid `TopoDS_Shape` handles
- **THEN** it SHALL fuse a and b, then fuse the result with c
- **THEN** it SHALL return the combined shape representing a ∪ b ∪ c

#### Scenario: Common three shapes
- **WHEN** `(kernel/common a b c)` is called with three valid `TopoDS_Shape` handles
- **THEN** it SHALL intersect a and b, then intersect the result with c
- **THEN** it SHALL return the combined shape representing a ∩ b ∩ c

#### Scenario: Cut three shapes
- **WHEN** `(kernel/cut a b c)` is called with three valid `TopoDS_Shape` handles
- **THEN** it SHALL subtract b from a, then subtract c from the result
- **THEN** it SHALL return the combined shape representing a − b − c

#### Scenario: Boolean with single extra arg
- **WHEN** `(kernel/fuse a b)` is called with exactly two shape handles
- **THEN** it SHALL behave identically to a binary fuse operation
- **WHEN** `(kernel/fuse a b)` returns a shape
- **THEN** `(kernel/fuse a b)` SHALL be equivalent to `(kernel/fuse a b)` with no `more` args

### Requirement: Input shapes survive boolean operation
The system SHALL NOT destroy or modify the input shapes passed to boolean operations.

#### Scenario: Input shapes remain valid after boolean
- **WHEN** `(kernel/fuse shape-a shape-b)` returns a result
- **THEN** shape-a SHALL still be valid and usable for further operations
- **THEN** shape-b SHALL still be valid and usable for further operations
