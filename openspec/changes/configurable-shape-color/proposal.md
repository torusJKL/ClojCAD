## Why

The default shape color (`0x4488cc` blue) is hardcoded in `shape_adapter.cljs` and cannot be changed without editing source code. Users need the ability to customize the default color via the REPL, browser console, and a persistent settings file — enabling different visual themes and faster iteration without recompilation.

## What Changes

- Change the default shape color from `0x4488cc` (blue) to `#fbd92c` (yellow/gold) in `build-part`
- Introduce a `ClojCAD.viewport.config` namespace with a `*default-shape-color*` atom and `set-default-shape-color!` function
- Expose `setShapeColor(color)` on `js/window` for browser console use
- Add an EDN settings file (`config.edn`) loaded at startup for persistent default color
- Thread the configurable default through `build-part` and `build-child-part` call sites in `scene/manager.cljs`

## Capabilities

### New Capabilities
- `default-shape-color`: Configurable default shape color that can be set via REPL, browser console, and persisted EDN settings file.

### Modified Capabilities

*(none — no existing specs are changing)*

## Impact

- `src/ClojCAD/viewport/shape_adapter.cljs` — `build-part` and `build-child-part` default color
- `src/ClojCAD/viewport/config.cljs` — new file with config atom and console API
- `src/ClojCAD/scene/manager.cljs` — thread default color through calls to `build-part` / `build-child-part`
- `src/ClojCAD/core.cljs` — load settings on startup
- `config.edn` — new top-level persistent settings file
