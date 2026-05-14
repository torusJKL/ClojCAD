## Context

All source code uses the top-level namespace `CADscript` (e.g., `CADscript.core`, `CADscript.kernel.api`, etc.) with source files under `src/CADscript/`. The `shadow-cljs.edn` entry point references `CADscript.core/init`. This is a pure rename — the code has no external consumers, no published artifacts, and no backward-compatibility constraints.

## Goals / Non-Goals

**Goals:**
- Rename all Clojure namespaces from `CADscript.xxx` to `ClojCAD.xxx`
- Rename the source directory from `src/CADscript/` to `src/ClojCAD/`
- Update `shadow-cljs.edn` `:init-fn` to `ClojCAD.core/init`
- Verify the project still compiles and runs after the rename

**Non-Goals:**
- No functional changes — no new features, no API changes, no refactoring
- No changes to archived change proposals in `openspec/changes/archive/`
- No changes to `openspec/specs/` (they reference conceptual capabilities, not namespace names)
- No changes to `package.json` name or other non-namespace identifiers

## Decisions

1. **Use simple find-and-replace for namespace references** — The rename is mechanical: `CADscript` → `ClojCAD` in `ns` forms, `:require` specs, and `:init-fn`. No semantic changes needed.

2. **Use `git mv` for directory renaming** — Preserves file history. Rename `src/CADscript/` → `src/ClojCAD/` in a single step.

3. **Do not rename archived change proposals** — Archived changes document what was built; they reference old namespace names for historical accuracy. Modifying them would create confusion and break the audit trail.

4. **Single-commit approach** — All changes are mechanical and can be applied atomically. No migration or rollback plan needed (project has no external consumers yet).

5. **Update `shadow-cljs.edn` only for `:init-fn`** — No other build configuration changes needed. The source path remains `"src"`.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Missed references in comments or docstrings | Search for all `CADscript` occurrences in source files |
| `git mv` fails on certain OS/filesystems | Use `mv src/CADscript src/ClojCAD` with git add/rm as backup |
| Macro file (`defmodel.clj`) uses fully-qualified `CADscript.model.core/reactive-model` | Included in the find-and-replace scope |
| External tooling configured for old namespace | None — no CI/CD, linters, or external tooling reference the namespace yet |
