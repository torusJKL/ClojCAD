## Why

Users need 3D text for labels, annotations, badges, and logos in CAD models. No existing primitive supports this. CascadeStudio demonstrates the approach (opentype.js → OCCT path conversion → extrusion) and the OCCT bindings in opencascade.js already provide all necessary geometry classes.

Separately, extrusion of 2D profiles is a fundamental CAD operation missing from the kernel. Text3D depends on it, and users need it independently.

## What Changes

- **`text3d`** (new): Generate 3D text from a string — `(text3d "Hello" 36 :font "Cousine" :height 0.15)` — returns a `TopoDS_Solid`
- **`extrude`** (new): Extrude a face along a vector — `(extrude face [0 0 10])` — wraps `BRepPrimAPI_MakePrism`
- **Font loading subsystem**: Bundled Cousine (Regular, Bold, Italic, BoldItalic) auto-loaded at init; `(register-font! name url)` for custom fonts with IndexedDB persistence
- **New dependency**: `opentype.js` npm package
- **New assets**: `public/fonts/Cousine-*.ttf` (4 variants) + `LICENSE.txt`
- **`kernel/api.cljs`**: Export `text3d`, `extrude`, `register-font!`

## Capabilities

### New Capabilities
- `text3d-primitive`: 3D text extrusion from strings via opentype.js font path parsing and OCCT wire/face/solid construction
- `extrude-primitive`: Linear extrusion of 2D faces via OCCT `BRepPrimAPI_MakePrism`
- `font-registry`: Font loading subsystem supporting bundled fonts, HTTP URL registration with IndexedDB persistence, and runtime registry lookup

## Impact

- `package.json` — add `opentype.js` dependency
- `src/ClojCAD/kernel/text3d.cljs` — new namespace: font loading, path→OCCT conversion, text3d function
- `src/ClojCAD/kernel/primitives.cljs` — add `extrude` function
- `src/ClojCAD/kernel/api.cljs` — three new exported symbols
- `src/ClojCAD/kernel/init.cljs` — piggyback font preloading on OCCT WASM init chain
- `public/fonts/` — 4 TTF files + LICENSE.txt (~600KB total)
- `test/ClojCAD/kernel/text3d_test.cljs` — new tests
- `test/ClojCAD/kernel/primitives_test.cljs` — extend with extrude tests
- No changes to tessellation, viewer, scene manager, or defmodel macro
