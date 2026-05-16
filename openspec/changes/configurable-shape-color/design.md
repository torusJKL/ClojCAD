## Context

The shape color defaults to hardcoded `0x4488cc` (blue) in `shape_adapter.cljs:30` when no `:color` key is provided in the opts map. Both `build-part` and `build-child-part` use this fallback. There is no way to change this default without editing source and recompiling.

The project already uses atoms for runtime configuration (`ClojCAD.scene.manager/params`, `ClojCAD.viewport.export_ui/*export-quality*`) and exposes functions on `js/window` for browser console access (`setExportQuality`, `setViewerTheme`). No persistent settings file exists yet.

## Goals / Non-Goals

**Goals:**
- Change the hardcoded default color `0x4488cc` to configurable value `#fbd92c`
- Provide an atom-based config mechanism in a new `ClojCAD.viewport.config` namespace
- Expose a `set-default-shape-color!` REPL-friendly function
- Expose `setShapeColor(color)` on `js/window` for browser console
- Persist the setting via an EDN file (`public/config.edn`) served as a static asset

**Non-Goals:**
- Per-instance color overrides (already supported via opts map)
- Color picker UI
- Per-model color configuration in `defmodel` metadata
- Multiple settings beyond default shape color

## Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Runtime state mechanism | Atom in new `config` namespace | Follows existing patterns (`params`, `*export-quality*`). Simple, predictable, REPL-friendly. |
| New namespace | `ClojCAD.viewport.config` | Keeps config concerns separate from shape rendering and export UI. Easy to extend with future settings. |
| EDN file location | `public/config.edn` | Served as a static asset from the dev server's web root (`public/`). Accessible via XHR at `/config.edn`. |
| EDN loading mechanism | XHR at startup (core.cljs) | No server-side. Simplest approach in browser ClojureScript. Falls back to default if file missing. |
| Console API name | `setShapeColor` | Matches `setExportQuality` / `setViewerTheme` naming convention (camelCase). |
| Color storage format | Hex integer (e.g. `0xfbd92c`) | Matches existing `0x4488cc` pattern in shape_adapter. Avoids parsing overhead. |
| Default value change | From `0x4488cc` to `0xfbd92c` | Requested by users for better visual contrast against dark theme. |

### Alternatives Considered

- **`:color` in defmodel metadata** — Rejected. This would couple visual concerns with model definitions. The opts map already supports per-instance overrides at display time.
- **CSS custom properties** — Rejected. Colors are used in WebGL/Three.js rendering, not CSS.
- **localStorage** — Rejected. EDN file is more idiomatic for Clojure and can be version-controlled.
- **Reagent/atom from config** — Rejected. No UI needs reactivity for this value; a plain Clojure atom suffices.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| `config.edn` may not exist (first run, fresh clone) | Graceful fallback: atom defaults to `0xfbd92c` if file not found or parse fails |
| Config loaded async via XHR — race with first `build-part` call | Load config in `core.cljs` before `demo/start-demo!`; block shape building if needed |
| Users expect EDN to be written back on change | Out of scope for initial implementation. Manual EDN editing only. |

## Open Questions

- None — the scope is narrow and well-understood.
