## Why

ClojCAD currently lacks a query API for inspecting what objects are displayed in the scene and their visibility/tag state. Users can show/hide individual models and tags, but there is no way to list objects, filter by tag or visibility, or perform bulk show/hide operations across all models. This makes the scene opaque and interactive workflows cumbersome as models grow in number and complexity.

## What Changes

- **`list-objects`**: Query all objects in the scene, with optional filter map (`:tag`, `:visibility`, `:name-matching`) for targeted queries
- **`list-tags`**: Enumerate all unique tag labels currently in the scene
- **`show-model` / `hide-model` (BREAKING)**: Overloaded to accept either a model name (existing behavior) or a filter map (`:tag`, `:model`, `:name-matching`) for tag visibility ops. Replaces `show-tag`, `hide-tag`, `show-by-tag`, `hide-by-tag`.
- **`toggle-model`**: Accepts a filter map (`:tag`, `:model`, `:name-matching`) — inverts visibility on matching models/tags
- **`show-all` / `hide-all`**: Bulk show or hide all models
- **`show-tag` / `hide-tag` (REMOVED)**: Subsumed by `show-model`/`hide-model` with map arg

## Capabilities

### New Capabilities
- `object-listing`: Scene object query API — list objects, filter by tag or visibility, enumerate tags

### Modified Capabilities
- `interactive-viewer`: Overload `show-model`/`hide-model` with filter map for tag visibility; add `toggle-model` and `show-all`/`hide-all`

## Impact

- **`src/ClojCAD/scene/`**: New `api.cljs` module exposing query and bulk-visibility functions; minor additions to `manager.cljs` for bulk operations
- **`src/ClojCAD/kernel/api.cljs`**: Re-export new public scene API functions
- **Tests**: New test file `test/ClojCAD/scene/api_test.cljs`
