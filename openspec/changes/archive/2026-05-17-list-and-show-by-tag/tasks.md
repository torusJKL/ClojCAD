## 1. Filter map dispatch in manager.cljs

- [x] 1.1 Add `matching-models` helper that takes a filter map (`:tag`, `:name-matching`) and returns matching `[name entry]` pairs from the scene atom
- [x] 1.2 Overload `show-model` to detect `(map? arg)` — symbol arg behaves as today; map arg dispatches to tag-level show
- [x] 1.3 Overload `hide-model` to detect `(map? arg)` — symbol arg behaves as today; map arg dispatches to tag-level hide
- [x] 1.4 Add `toggle-model` function accepting a filter map (`:tag`, `:model`, `:name-matching`)
- [x] 1.5 Remove `show-tag` and `hide-tag` functions
- [x] 1.6 Add `show-all` and `hide-all` functions
- [x] 1.7 Update `notify-callback` to use `show-model`/`hide-model` instead of direct `swap!` (optional consistency)

## 2. Query API in scene.api

- [x] 2.1 Implement `glob->regex` helper that converts `"foo*"` patterns to `js/RegExp`
- [x] 2.2 Create `src/ClojCAD/scene/api.cljs` requiring `ClojCAD.scene.manager`
- [x] 2.3 Implement `list-objects` with optional filter map (`:tag`, `:visibility`, `:name-matching`)
- [x] 2.4 Implement `list-tags` returning unique tag labels as a set of keywords
- [x] 2.5 Forward `show-model`, `hide-model`, `toggle-model`, `show-all`, `hide-all` from manager

## 3. Tests

- [x] 3.1 Update `manager_test.cljs` — remove `show-tag`/`hide-tag` tests; add tests for overloaded `show-model`/`hide-model` map dispatch and `toggle-model`
- [x] 3.2 Create `api_test.cljs` with tests for `list-objects` (no filter, by tag, by visibility, by name-matching, combined) and `list-tags`
- [x] 3.3 Run tests and verify all pass

## 4. Verify

- [x] 4.1 Confirm `demo.cljs` compiles/works after removing `show-tag`/`hide-tag`
- [x] 4.2 Run full test suite
