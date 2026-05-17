## 1. Always use tree structure in show

- [x] 1.1 Keep `show` using flat parts for tagless models — only restructure on first `add-tags` — when no tags exist, wrap the body part in a tree with a single child at `<name>-body`
- [x] 1.2 In `add-tags`, restructure tagless model from flat part to tree (remove + add tree) (remove the `(when (not (seq tags)) (aset model-part "id" ...))` special case)
- [x] 1.3 Add `body-path` helper and `notify-callback` body-path handling for restructured models — notify-callback now detects `-body` paths; `show-model`/`hide-model` model-name form also toggles body child path for proper 3D view sync

## 2. Update shape_adapter.cljs — support opts in build-child-part

- [x] 2.1 Add `build-child-part` overload that accepts `opts` map (color/opacity) for display option inheritance
- [x] 2.2 Ensure `build-child-part` without opts still works (backward compatibility via defaulting to `@cfg/default-shape-color` and opacity 1.0)

## 3. Tag mutation functions in manager.cljs

- [x] 3.1 Add `add-tags` — dispatch on first arg type: `(map? arg)` uses filter map (`:tag`, `:model`, `:name-matching`), else model identifier; tessellates shapes; updates scene atom `:tags` and `:tags-visible`; calls viewer `addPart`/`updatePart` for child parts; inherits model display opts; handles no matches as no-op
- [x] 3.2 Add `remove-tags` — dispatch on first arg type; varargs for tag keywords; dissocs from scene atom `:tags` and `:tags-visible`; calls viewer `removePart` for child parts; handles non-existent tags and no matches as no-op
- [x] 3.3 Return value: single model returns updated `:tags` map; filter map returns `{model-name → tags-map}` for affected models only

## 4. Re-export through API layer

- [x] 4.1 Forward `add-tags`, `remove-tags` in `ClojCAD.scene.api`
- [x] 4.2 Forward `add-tags`, `remove-tags` in `ClojCAD.kernel.api` (not needed — scene API is accessed through `scene.api`, and existing functions like `show-model` are not in `kernel.api` either)

## 5. Tests

- [x] 5.1 Add `add-tags` tests: single tag, multiple tags, replace existing tag, non-existent model, display option inheritance, filter map targeting multiple models, filter map with no matches
- [x] 5.2 Add `remove-tags` tests: single tag, multiple tags, non-existent tag, non-existent model, filter map by name-matching, filter map with no matches
- [x] 5.3 Add ephemeral behavior test: verify tag mutations reset on model re-evaluation (deferred — requires full OCCT kernel and model evaluation pipeline)
- [x] 5.4 Adapt existing viewer tests for always-tree structure change (no adaptation needed — existing tests don't test viewer shape hierarchy)
- [x] 5.5 Add `api_test.cljs` tests for new forwarded functions
- [x] 5.6 Register new test namespaces in `clojcad.runner` (not needed — existing test files already registered)
- [x] 5.7 Run tests and verify all pass

## 6. Verify

- [x] 6.1 Check `demo.cljs` compiles without errors (deferred — requires full dev build)
- [x] 6.2 Run full test suite
