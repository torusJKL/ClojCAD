## Why

Tags on models are currently fixed at definition time — once a model is evaluated, its tagged sub-shapes are immutable. Users have no way to add new tagged sub-shapes or remove existing ones at runtime, limiting interactive workflows where models need dynamic shape composition.

## What Changes

- **`add-tags`**: Add new tag-key → shape pairs to an existing model instance. Shapes are tessellated and displayed as new child parts under the model.
- **`remove-tags`**: Remove existing tags from a model instance. Tagged child parts are removed from the scene.
- **Internal scene updates**: Tag changes trigger re-tessellation of new shapes, viewer child part updates, and scene atom synchronization.

## Capabilities

### New Capabilities
- `tag-mutation`: Runtime API for adding and removing tagged sub-shapes on model instances — `add-tags`, `remove-tags`

### Modified Capabilities
- `interactive-viewer`: Scene manager must handle dynamic addition/removal of tagged child parts via viewer API (`addChildPart`/`removeChildPart`); `tags-visible` map must stay in sync

## Impact

- **`src/ClojCAD/scene/manager.cljs`**: New `add-tags`, `remove-tags` functions; tag lifecycle management (tessellation, viewer sync)
- **`src/ClojCAD/scene/api.cljs`**: Re-export new tag mutation functions as public API
- **`src/ClojCAD/kernel/api.cljs`**: Re-export new public API functions
- **`src/ClojCAD/viewport/shape_adapter.cljs`**: May need new helpers for building a single child part (not the full tree) for incremental updates
- **Tests**: New test file `test/ClojCAD/scene/tag_mutation_test.cljs`
