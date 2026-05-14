## Why

The project has outgrown the "CADScript" name — it's no longer a script-oriented CAD tool but a full ClojureCAD platform. The name "ClojCAD" better reflects both the language (Clojure) and domain (CAD) while being shorter, more memorable, and distinct. The old namespace creates confusion in documentation, REPL usage, and publishing.

## What Changes

- **BREAKING**: Rename top-level namespace `CADscript` → `ClojCAD` across all source files
- **BREAKING**: Rename source directory `src/CADscript/` → `src/ClojCAD/`
- Update `shadow-cljs.edn` `:init-fn` to `ClojCAD.core/init`
- Update all internal `ns` forms and `:require` references
- Update any documentation or config files referencing `CADscript` namespaces (excluding archived change proposals in `openspec/changes/archive/`)
- Keep `package.json` `name` field as-is (it's an internal identifier, not user-facing)

No functional changes — this is pure namespace and directory renaming.

## Capabilities

### New Capabilities

None — no new capabilities are being introduced.

### Modified Capabilities

None — no spec-level requirements are changing. This is purely a rename with zero behavioral change.

## Impact

| Area | Impact |
|------|--------|
| `src/CADscript/` → `src/ClojCAD/` | Rename directory tree; all 14 source files affected |
| `shadow-cljs.edn` | Update `:init-fn CADscript.core/init` → `ClojCAD.core/init` |
| All `(ns CADscript.xxx ...)` forms | Rename to `(ns ClojCAD.xxx ...)` |
| All `:require` references to `CADscript.xxx` | Rename to `ClojCAD.xxx` (14 files, ~49 references) |
| `openspec/changes/archive/` | Leave untouched — historical artifacts should not be modified |
| `openspec/specs/` | No changes — specs reference conceptual capabilities, not implementation namespaces |
