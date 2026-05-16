## 1. Add opentype.js dependency

- [x] 1.1 `npm install opentype.js`
- [x] 1.2 Verify shadow-cljs can resolve the module (`:require ["opentype.js" :default opentype]`)
- [x] 1.3 Run `npm test` to confirm existing tests still pass

## 2. Create font registry namespace (`src/ClojCAD/kernel/font.cljs`)

- [x] 2.1 Create `ns ClojCAD.kernel.font` with `["opentype.js" :as opentype]` require
- [x] 2.2 Define `(defonce registry (atom {}))` — atom mapping font-name string to opentype.js Font object
- [x] 2.3 Define bundled fonts list:
      ```clojure
      (def ^:private bundled-fonts
        [{:name "Cousine"         :url "/fonts/Cousine-Regular.ttf"}
         {:name "Cousine-Bold"    :url "/fonts/Cousine-Bold.ttf"}
         {:name "Cousine-Italic"  :url "/fonts/Cousine-Italic.ttf"}
         {:name "Cousine-BoldItalic" :url "/fonts/Cousine-BoldItalic.ttf"}])
      ```
- [x] 2.4 Implement `(load-bundled-fonts!)` — fetch each TTF, parse with `opentype.parse`, swap! into registry
- [x] 2.5 Implement `(register-font! name url)` — fetch TTF from URL, parse, add to registry, persist in IndexedDB
- [x] 2.6 Implement `(lookup-font name)` — get font from registry atom (returns nil if not found)
- [x] 2.7 Implement `(list-fonts)` — returns sequence of registered font names `(keys @registry)`
- [x] 2.8 Implement `(font-info name)` — returns map with `:name`, `:source` (`:bundled` or `:custom`), and opentype.js metadata (`:family`, `:style`, `:glyphs`); returns nil if font not found
- [x] 2.9 Implement IndexedDB persistence layer:
      - [x] 2.9.1 Open IndexedDB database "clojcad-fonts" with object store "fonts"
      - [x] 2.9.2 Store font name → ArrayBuffer on register
      - [x] 2.9.3 On init, read all stored entries, parse via opentype, populate registry
      - [x] 2.9.4 Handle errors gracefully (private browsing, quota)
- [x] 2.10 Ensure `register-font!` returns a promise that resolves when registration completes

## 3. Piggyback font loading on kernel init (`src/ClojCAD/kernel/init.cljs`)

- [x] 3.1 Add `:require [ClojCAD.kernel.font :as font]`
- [x] 3.2 After OCCT WASM resolves, chain font loading:
      ```clojure
      (.then (fn [oc]
               (reset! oc-instance oc)
               (-> (font/load-bundled-fonts!)
                   (.then #(reset! loading? false)))))
      ```
- [x] 3.3 Handle font loading failure — set `loading?` to false regardless (fonts may fail, OCCT succeeded)

## 4. Implement path-to-OCCT geometry helpers (`src/ClojCAD/kernel/text3d.cljs`)

- [x] 4.1 Create `ns ClojCAD.kernel.text3d` with requires for `font`, `primitives`, `lifecycle`, `init`
- [x] 4.2 Implement `(pnt x y)` helper — creates `gp_Pnt_3(x, y, 0)`
- [x] 4.3 Implement `(line-edge p1 p2)` — `GC_MakeSegment_1(p1, p2)` → `BRepBuilderAPI_MakeEdge_24(Handle_Geom_Curve)` → edge
- [x] 4.4 Implement `(quad-edge p1 cp p2)` — `Geom_BezierCurve_1(3 points)` → edge
- [x] 4.5 Implement `(cubic-edge p1 cp1 cp2 p2)` — `Geom_BezierCurve_1(4 points)` → edge
- [x] 4.6 Implement `(add-edge-to-wire wire-builder edge)` — wrap in `BRepBuilderAPI_MakeWire_2`, add to builder via `Add_2`
- [x] 4.7 Implement `(commands->face commands)` — iterate opentype.js command array, build wires, handle M/L/Q/C/Z, close contours, handle holes via MakeFace_15 / MakeFace_22
- [x] 4.8 Wrap face construction in try/catch with warning for malformed glyphs

## 5. Implement text3d function

- [x] 5.1 `(text3d text size & {:keys [font height] :or {font "Cousine" height 0.15}})` — main entry point
- [x] 5.2 Look up font from registry via `font/lookup-font`; return nil if not found with warning
- [x] 5.3 Call `font.getPath(text, 0, 0, size).commands` to get SVG path commands
- [x] 5.4 Convert commands to face via `commands->face`
- [x] 5.5 If height > 0, extrude via `primitives/extrude` along [0, 0, height * size]
- [x] 5.6 Rotate extruded shape -90° around X axis via `primitives/rotate`
- [x] 5.7 Return the shape (or the face if height = 0)

## 6. Add extrude primitive (`src/ClojCAD/kernel/primitives.cljs`)

- [x] 6.1 Implement `(extrude face [dx dy dz])` wrapping `BRepPrimAPI_MakePrism_1`
- [x] 6.2 Handle nil face by returning nil
- [x] 6.3 Handle short extrusion vector (all zeros) by returning the face unchanged (or nil with warning)

## 7. Wire into public API (`src/ClojCAD/kernel/api.cljs`)

- [x] 7.1 Add `:require [ClojCAD.kernel.text3d :as text3d]`
- [x] 7.2 Add `:require [ClojCAD.kernel.font :as font]`
- [x] 7.3 Export: `(def text3d text3d/text3d)`
- [x] 7.4 Export: `(def extrude primitives/extrude)`
- [x] 7.5 Export: `(def register-font! font/register-font!)`
- [x] 7.6 Export: `(def list-fonts font/list-fonts)`
- [x] 7.7 Export: `(def font-info font/font-info)`

## 8. Bundle Cousine font files

- [x] 8.1 Create `public/fonts/` directory
- [x] 8.2 Download Cousine-Regular.ttf, Cousine-Bold.ttf, Cousine-Italic.ttf, Cousine-BoldItalic.ttf
- [x] 8.3 Create `public/fonts/LICENSE.txt` with Apache 2.0 license and "Cousine font by Steve Matteson" attribution
- [x] 8.4 Add `public/fonts/` to version control (git add)
- [x] 8.5 Verify fonts are served at `/fonts/Cousine-Regular.ttf` in dev server

## 9. Tests

- [x] 9.1 Add `test/ClojCAD/kernel/text3d_test.cljs`:
      - [x] 9.1.1 Test `text3d` with default args returns non-null shape
      - [x] 9.1.2 Test `text3d` with custom font name returns non-null shape
      - [x] 9.1.3 Test `text3d` with unknown font returns nil
      - [x] 9.1.4 Test `text3d` with zero height returns a face (not extruded)
      - [x] 9.1.5 Test `text3d` with letter "O" produces a solid with correct topology
- [x] 9.2 Extend `test/ClojCAD/kernel/primitives_test.cljs`:
      - [x] 9.2.1 Test `extrude` a face returns non-null shape
      - [x] 9.2.2 Test `extrude` with nil face returns nil
      - [x] 9.2.3 Test `extrude` with zero vector (handled gracefully)
- [x] 9.3 Add tests for `list-fonts` and `font-info`:
      - [x] 9.3.1 Test `list-fonts` returns sequence including bundled fonts
      - [x] 9.3.2 Test `font-info` returns map with `:name` and `:source` for registered fonts
      - [x] 9.3.3 Test `font-info` returns nil for unknown font
- [x] 9.4 Run `npm test` to verify all tests pass

## 10. Update README

- [x] 10.1 Add `text3d` to DSL Reference with examples
- [x] 10.2 Add `extrude` to DSL Reference
- [x] 10.3 Document `register-font!` usage
- [x] 10.4 Document `list-fonts` and `font-info` for font introspection
- [x] 10.5 Update dependencies section to mention opentype.js and Cousine font

## 11. Implement synchronous `load-font!`

- [x] 11.1 Implement `(load-font! name url)` using synchronous XHR (`responseType: "arraybuffer"`)
- [x] 11.2 Parse buffer with `opentype.parse`, register in registry atom
- [x] 11.3 Persist to IndexedDB asynchronously (fire-and-forget)
- [x] 11.4 Return font object on success, nil on failure
- [x] 11.5 Export from `kernel/api.cljs`
- [x] 11.6 Document in README, note the difference from `register-font!`
