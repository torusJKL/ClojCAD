## Why

A parametric CAD tool with a ClojureScript DSL and opencascade.js WebAssembly binding enables REPL-driven design where shape changes appear instantly in a web viewer. This PoC validates the fundamental architecture: ClojureScript + nrebel + opencascade.js in the browser, with a shared params atom driving incremental model re-evaluation.

## What Changes

- New shadow-cljs project with opencascade.js and Three.js as npm dependencies
- `defmodel` macro that expands to a reactive-model with parameter extraction and scene registration
- `show` function for idempotent model display (repeated calls update existing layer)
- `tag` macro for opt-in intermediate geometry labeling
- Scene manager that watches a shared params atom, diffs changed keys, and re-evaluates only dirty models
- Direct Three.js viewport (no Reagent in the 3D canvas) with layer panel built in Reagent
- Minimal PoC: display a single sphere, change its radius at the REPL, see live update

## Capabilities

### New Capabilities
- `parametric-model`: Define parametric shapes via `defmodel` macro with reactive re-evaluation on param change
- `interactive-viewer`: Three.js web viewport with orbit controls, layer panel, and live mesh updates
- `cad-geometry-kernel`: opencascade.js WebAssembly integration for B-Rep geometry operations

### Modified Capabilities

None — first capability set.

## Impact

New project under `src/` with shadow-cljs build configuration. Dependencies: opencascade.js, Three.js, Reagent (for UI chrome only), shadow-cljs. No existing code is modified.
