## Context

The kernel currently provides 3D primitives (sphere, box, cylinder, cone), 2D primitives (circle, polygon), and transforms (translate, rotate). Missing are:

1. **Text3D** — convert a string into 3D geometry
2. **Extrude** — linearly extrude a 2D face into a solid

The zalo opencascade.js fork bundles all OCCT classes needed. CascadeStudio's `Text3D` in `StandardLibrary.js` demonstrates the end-to-end approach. This design adapts it to ClojureScript with an extensible font registry and a standalone `extrude` primitive.

## Goals / Non-Goals

**Goals:**
- `text3d` function that takes a string, size, and optional font/height
- `extrude` function that extrudes a face along a vector
- Bundled Cousine font (Regular, Bold, Italic, BoldItalic) preloaded at init
- `register-font!` to add custom fonts via HTTP URL, persisted in IndexedDB
- Font registry lookup: font name → opentype.js Font object
- Proper lifecycle tracking for all OCCT allocations
- WASM-dependent tests

**Non-Goals:**
- File picker UI for font loading (deferred — URL+IDB covers the common case)
- OS font table API (`queryLocalFonts` — Chrome-only, deferred)
- 2D Boolean operations on wire/face shapes
- Chamfer/Fillet on text edges
- Multi-line text or text layout
- Right-to-left text or complex script shaping (opentype.js supports it, but not a focus)

## Decisions

### 1. Font loading piggybacks on OCCT init

In `init.cljs`, after OCCT WASM loads, the same promise chain loads bundled fonts via opentype.js. Both are async; both must complete before model evaluation.

```
init-kernel
├── load opencascade.js WASM (async)
│     └── resolve → oc-instance set
└── load bundled fonts (async, after OCCT)
      ├── opentype.js dynamic import
      ├── fetch Cousine-*.ttf → opentype.parse → registry
      └── resolve → all bundled fonts ready
```

Custom fonts are loaded later via `register-font!` and may not be available for the first evaluation. If a requested font isn't in the registry, `text3d` returns nil.

### 2. Font registry as an atom

```clojure
(defonce registry (atom {}))  ;; {name -> opentype.js Font object}
```

Singleton atom. Bundled fonts populate it at init. `register-font!` adds to it. `text3d` looks up from it.

### 3. `register-font!` fetches and persists

```clojure
(register-font! "Mine" "https://.../Mine.ttf")
;; 1. Fetch TTF bytes over HTTP
;; 2. opentype.parse(array-buffer) → font object
;; 3. Store in registry atom
;; 4. Store bytes + name in IndexedDB
;;
;; On next page load:
;; 5. Init checks IndexedDB
;; 6. opentype.parse(stored-bytes) → font object
;; 7. Add to registry atom
;; → No dialog, no refetch, works offline
```

IndexedDB key/value: `font-name → ArrayBuffer`. Deserialization on load is O(n) for n registered fonts (negligible — each is a `parse(buffer)` call).

### 3b. `load-font!` — synchronous alternative

```clojure
(load-font! "Mine" "/fonts/Mine.ttf")
;; 1. Synchronous XHR (XMLHttpRequest with async=false)
;; 2. responseType = "arraybuffer" → ArrayBuffer
;; 3. opentype.parse(array-buffer) → font object
;; 4. Store in registry atom
;; 5. Fire-and-forget IndexedDB persistence (async, non-blocking)
;; → Returns font object or nil
```

Uses synchronous `XMLHttpRequest` with `responseType: "arraybuffer"` to block until the font file is fetched. This avoids the async gap problem of `register-font!` where `text3d` may return nil if called before the promise resolves. IndexedDB persistence still runs in the background.

### 4. Font inspection API: `list-fonts` and `font-info`

Two lightweight introspection functions:

```clojure
(defn list-fonts [] (keys @registry))
;; => ("Cousine" "Cousine-Bold" "Cousine-Italic" "Cousine-BoldItalic")

(defn font-info [name]
  (when-let [font (lookup-font name)]
    {:name name
     :source (if (contains? bundled-names name) :bundled :custom)
     :family (.-names.fontFamily font)
     :style (.-names.fontSubfamily font)
     :glyphs (.-length (.-glyphs font))}))
;; => {:name "Cousine" :source :bundled :family "Cousine" :style "Regular" :glyphs 2200}
```

`font-info` exposes opentype.js metadata. The `:source` key lets users distinguish bundled fonts (always available) from custom ones (may fail to restore from IndexedDB).

### 5. text3d: path commands → OCCT geometry

The core loop translates opentype.js path commands into OCCT wires and faces:

| Command | OCCT equivalent |
|---------|-----------------|
| `M (x, y)` | Record firstPoint/lastPoint, start new `BRepBuilderAPI_MakeWire` |
| `L (x, y)` | `GC_MakeSegment_1(last, next)` → `BRepBuilderAPI_MakeEdge_24(Handle_Geom_Curve)` → `BRepBuilderAPI_MakeWire_2(edge)` |
| `Q (x1, y1, x, y)` | `Geom_BezierCurve_1(3 points)` → edge → wire |
| `C (x1, y1, x2, y2, x, y)` | `Geom_BezierCurve_1(4 points)` → edge → wire |
| `Z` | Close contour. If first contour: `BRepBuilderAPI_MakeFace_15(wire)`. If subsequent: `BRepBuilderAPI_MakeFace_22(existingFace, newWire)` for holes. |

All points use `gp_Pnt_3(x, y, 0)`.

### 6. Multiple contours per glyph

Letters like "O", "A", "B", "D" have inner contours (holes). The approach:

1. First `Z` → create base face via `MakeFace_15(wire)`, push to `textFaces` vector
2. Subsequent `Z` for the *same glyph* → use `MakeFace_22(textFaces[last], newWire)` to add as hole
3. At "M" starting a *new glyph* (different firstPoint), start fresh

CascadeStudio handles this by accumulating `textFaces` and using `MakeFace_22` with the last face in the list. This relies on the fact that opentype.js outputs contours for a single glyph consecutively before moving to the next glyph.

### 7. Extrude and post-processing

The final face for each glyph or the accumulated face is extruded:

```clojure
;; Extrude face along Z by height * size
(let [prism (BRepPrimAPI_MakePrism_1 face (gp_Vec_4 0 0 (* height size)))]
  (.Shape prism))

;; Rotate -90° around X to stand text upright
(rotate extruded 1 0 0 -90)
```

`extrude` as a standalone primitive:

```clojure
(defn extrude [face direction]
  (let [ctor (.-BRepPrimAPI_MakePrism_1 (oc))
        builder (js/Reflect.construct ctor #js [face
                  (js/Reflect.construct (.-gp_Vec_4 (oc)) #js direction)])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))
```

### 8. Module organization

- `src/ClojCAD/kernel/text3d.cljs` — font registry, font loading, register-font!, text3d
- `src/ClojCAD/kernel/primitives.cljs` — add `extrude`
- `src/ClojCAD/kernel/api.cljs` — re-exports for text3d, extrude, register-font!
- `src/ClojCAD/kernel/init.cljs` — add font preloading to init chain
- `test/ClojCAD/kernel/text3d_test.cljs` — text3d tests
- `test/ClojCAD/kernel/primitives_test.cljs` — extend with extrude tests

### 9. Backward compatibility

- No existing functions change
- `extrude` is a new function, doesn't affect any existing primitive
- New dependency (`opentype.js`) doesn't affect existing functionality
- IndexedDB is lazily checked on load — no migration needed

## Risks / Trade-offs

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| `GC_MakeSegment`/`Geom_BezierCurve` constructor suffixes may differ in zalo fork | Low | Check against `opencascade.idl` in the fork; fall back to alternate suffixes. CascadeStudio already uses these exact suffixes successfully. |
| IndexedDB unavailable (private browsing, storage quota) | Low | `register-font!` still works in-session; persistence is best-effort. Catch errors gracefully. |
| Opentype.js `getPath` output varies by font (some use only Q, some C, some both) | Low | Code handles all five command types — same set as SVG path `d` attribute. Any valid TTF works. |
| Self-intersecting font contours cause OCCT face construction failure | Low | CascadeStudio wraps MakeFace in try/catch. Follow same pattern — catch and warn. |
| Large font files (>1MB) slow down init | Low | Cousine Regular is ~150KB. Bundle is ~600KB total for 4 variants. Acceptable. |
| `MakeFace_22` constructor may not accept wires with reversed orientation for holes | Medium | May need to reverse inner contour wires (`wire.Reversed()`) before passing to MakeFace. Test with letters containing holes (O, A). |

## Open Questions

- How to signal to the model system that a font isn't loaded yet and trigger re-evaluation when it is? (CascadeStudio clears the arg cache. Our `reactive-model` caches by params — the nil return would be cached. Might need a way to invalidate.)
- Which opentype.js module path should be used for dynamic `import()` in the shadow-cljs build?
