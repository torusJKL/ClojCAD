## Context

Tags in ClojCAD are currently static — defined within a `defmodel` body via the `tag` function, collected during model evaluation, and stored in the scene atom's `:tags` map. Once a model is displayed, there is no mechanism to add new tagged sub-shapes or remove existing ones without re-evaluating the entire model. The recent `list-and-show-by-tag` work added query and visibility APIs but did not address tag mutation.

The scene atom stores shape data for tags as tessellated meshes, and the viewer displays them as child parts under the parent model path (`/<model-name>/<tag-label>`). Any tag mutation must synchronize three layers: the scene atom, the viewer's part tree, and the tessellation pipeline.

## Goals / Non-Goals

**Goals:**
- Provide `add-tags` to append new tagged sub-shapes to an existing model instance
- Provide `remove-tags` to delete tagged sub-shapes from an existing model instance
- New tags are tessellated and displayed as child parts in the viewer
- Removed tags are cleaned up from the scene atom and viewer
- Inherit the model's display options (color, opacity) for new tag shapes

**Non-Goals:**
- Modify the `defmodel` macro or model definition DSL
- Re-evaluate or re-tessellate existing tags that are not being changed
- Persist tag mutations across model re-evaluation (param changes will reset tags to model-definition defaults)
- Namespace collision detection for tag labels (same-label tags silently replace)

## Decisions

**Decision 1: Tag mutation operates on the scene atom, not the model registry**
Tags are stored in the scene atom per model instance. Mutations update the scene atom directly, then sync to the viewer. This avoids coupling to the model evaluation pipeline and keeps the API consistent with the existing `show-model`/`hide-model` visibility functions.
- *Alternative considered*: Re-evaluating the model with updated tag definitions. Rejected because it would re-run the entire model function, potentially causing side effects and losing the clean separation between model definition and scene state.

**Decision 2: Tag mutation functions dispatch on first-arg type — map for filter, symbol/keyword for model name**
The first argument follows the same convention as `show-model`/`hide-model`: a map (`:tag`, `:model`, `:name-matching`) selects matching models; a keyword or symbol identifies a single model by name. This enables targeted operations like `(remove-tags {:tag :sphere :name-matching "temp*"})` to remove a tag from all models matching a pattern.
- `add-tags`: model-identifier form — `(add-tags 'my-model {:label shape})`; filter-map form — `(add-tags {:name-matching "temp*"} {:warning (make-sphere 1)})` adds the same tag/label to all matching models (each tessellated independently).
- `remove-tags`: model-identifier form — `(remove-tags 'my-model :sphere)`; filter-map form — `(remove-tags {:name-matching "temp*"} :sphere)` removes the tag from matching models.
- *Alternative considered*: Model-identifier only. Rejected because filter-map dispatch aligns with the existing scene API convention and avoids forcing callers to iterate and compose manually.

**Decision 3: Return value changes based on dispatch form**
With a single model identifier, return the updated `:tags` map for that model. With a filter map, return a map of `model-name → tags-map` for all affected models.
- *Alternative considered*: Always returning a map. Rejected because the single-model form is the common case and a direct tags-map return is more ergonomic for chaining and inspection.

**Decision 4: `add-tags` accepts tag-keyword → OCCT-shape maps**
Shapes are OCCT shapes (not pre-tessellated meshes), so the scene manager handles tessellation internally — consistent with how `show` processes model output. Tag keywords must be unique per model; adding a tag that already exists replaces it.
- *Alternative considered*: Accepting pre-tessellated meshes. Rejected because it breaks the abstraction that tessellation is the scene manager's responsibility.

**Decision 5: Tags not present for removal are silently ignored**
If a tag to remove doesn't exist on a model, it's a no-op. This makes removal idempotent.
- *Alternative considered*: Throwing on non-existent tags. Rejected because idempotent removal is more ergonomic for interactive workflows.

**Decision 6: Tag mutations are ephemeral — reset on model re-evaluation**
When a model is re-evaluated (due to param changes), the scene entry is recomputed from scratch, including its `:tags` map. Mutated tags are not preserved. This keeps the system simple and avoids stale state.
- *Alternative considered*: Persisting mutations across re-evaluations. Rejected because it introduces complex merge semantics and violates the principle that model output is a pure function of its parameters.

**Decision 7: Always use tree structure in `show` — even for tagless models**
Currently `show` adds a model as a flat part (`/root/<name>`) when it has no tags, or a tree part (`/root/<name>` with children) when it has tags. To support `add-tags` on any model, `show` SHALL always use `build-shapes-tree` with a single body child (no tag children) for tagless models. This ensures the viewer hierarchy always supports child parts.
- *Consequence*: Tagless models will have a body child at `/root/<name>/<name>-body` instead of the flat part at `/root/<name>`. Tree panel will show an expandable entry with one child for tagless models — a minor cosmetic change.
- *Alternative considered*: Restructuring on first `add-tags`. Risk of viewer incompatibility. Rejected.

**Decision 8: Added tags default to origin position**
Runtime-added tags use `[0 0 0]` as their position offset (matching the default in `build-child-part`). If position support is needed later, the `add-tags` second argument can be extended to accept `{tag-keyword {:shape s :pos [x y z]}}` without breaking the simple `{tag-keyword shape}` form.

## Risks / Trade-offs

- **[Restructuring tagless models]** Changing `show` to always use tree structure may cause a brief flash when existing tagless models are re-evaluated (part path changes from flat to tree) → Mitigation: This is a one-time transition. New models are always trees from the start.
- **[Stale viewer state]** If a tag is removed while the viewer notification is pending, the child part removal call may fail → Mitigation: Wrap viewer calls in try/catch and log warnings. Scene atom is the source of truth.
- **[Performance on large models]** Tessellating a new tag shape synchronously could block the UI thread → Mitigation: Keep tessellation synchronous (matching existing scene manager conventions) but document that large shapes should be added during model definition for reactivity.
- **[Tag collision with defmodel tags]** If a user adds a tag label that already exists from the model definition, the new shape replaces the old one silently → Trade-off: Acceptable for now. A future warning could be added for duplicate tags.
- **[Model re-evaluation blows away mutations]** Users may be surprised when param changes reset their tag mutations → Mitigation: Clearly document this behavior. It's consistent with how ClojCAD treats all model state as a function of params.
