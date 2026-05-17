## Context

The `ClojCAD.scene.manager` namespace owns the `scene` atom — a map of displayed model names to their state (mesh, tags, visibility, opts). It exposes individual show/hide operations (`show-model`, `hide-model`, `show-tag`, `hide-tag`) but no query or bulk operations.

The `ClojCAD.kernel.api` namespace is the public API for kernel operations — it should not mix in scene-layer concerns.

## Goals / Non-Goals

**Goals:**
- Provide a single `list-objects` function that returns all objects when called with no args, and accepts an optional map of filter criteria (`:tag`, `:visibility`, `:name-matching`)  
- Provide bulk visibility operations: show/hide/toggle by tag across all models, show all / hide all
- Provide `list-tags` to enumerate unique tag labels across all models
- Keep bulk operations efficient — O(n) in number of models, single viewer batch where possible

**Non-Goals:**
- Tag CRUD (create/update/delete tags on models) — tags are defined inside `defmodel` bodies
- Tag inheritance or hierarchy — tags are flat labels on sub-shapes
- Undo/redo for visibility changes — existing pattern (direct atom mutation) is preserved
- Dedicated UI for listing/filtering — this is a programmatic API only

## Decisions

1. **New `ClojCAD.scene.api` namespace** — query functions (`list-objects`, `list-tags`) and forwarding wrappers for visibility ops live here. `manager.cljs` remains the owner of viewer interactions.

2. **No re-export through `kernel/api.cljs`** — scene and kernel are separate layers. Users require `ClojCAD.scene.api` independently.

3. **`show-model` / `hide-model` overloaded with map dispatch** — When given a symbol or keyword, they behave as today (whole-model visibility). When given a map, they operate on tags:
   - `{:tag :sphere}` — act on tag across all models
   - `{:tag :sphere :model 'my-model}` — act on tag on a specific model (replaces `show-tag`/`hide-tag`)
   - `{:tag :sphere :name-matching "foo*"}` — act on tag on name-matching models
   - Detection: `(map? arg)` distinguishes map from symbol/keyword. Maps are never valid model identifiers.

4. **`show-tag` / `hide-tag` removed** — subsumed by `(show-model {:tag :sphere :model 'my-model})` / `(hide-model {:tag :sphere :model 'my-model})`.

5. **`toggle-model` added** — accepts same filter map format (`:tag`, `:model`, `:name-matching`). Separate function because there's no natural "toggle" overload for `show-model`/`hide-model`.

6. **`list-objects` with optional filter map**:
   - `(list-objects)` — returns all objects (no filter)
   - `(list-objects {:tag :sphere})` — filter by tag label
   - `(list-objects {:visibility :visible})` — filter by visibility status
   - `(list-objects {:name-matching "foo*"})` — filter model names matching a pattern
   - `(list-objects {:tag :sphere :visibility :visible :name-matching "foo*"})` — combined filters
   - `(list-tags)` — separate function returning unique tag labels as a set of keywords

7. **`name-matching` accepts strings with glob-style patterns** — `"foo*"`, `"*bar"`, `"*baz*"`. Implemented via JavaScript `RegExp` conversion from simple glob to regex (escape dots, replace `*` with `.*`, wrap in `^...$`). Also accepts a native `js/RegExp` directly.

8. **Sharing filter logic** — `list-objects` and the visibility ops share a common `matching-models` helper that takes the scene atom and a filter map, returning only the entries that satisfy all present filters (`:tag`, `:name-matching`, `:visibility`). This lives in `scene.api` and is used by both query and visibility functions.

9. **Return plain Clojure data structures** — `list-objects` returns the scene atom content; filters return subsets. No new record types or protocols.

## Risks / Trade-offs

- **Race with viewer sync** → Bulk operations call viewer methods synchronously per model, same as existing `show-model`/`hide-tag`. Could batch viewer calls in the future if needed.
- **Scene atom shape is not validated** → Query functions deref the atom and assume the schema. Malformed entries (e.g., missing `:visible?`) return nil/default values rather than crashing.
- **Tags are string keys in the atom** → Internally tags are stored as strings in the scene atom (from `name` conversion). Query functions accept keywords and convert with `name`.
