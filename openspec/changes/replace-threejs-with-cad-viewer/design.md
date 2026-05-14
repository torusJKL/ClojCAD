## Context

The current PoC viewport layer is ~125 lines of hand-rolled Three.js spread across four files:

```
viewport/scene.cljs       — Scene, Camera, Renderer, lights, grid, axes
viewport/controls.cljs    — OrbitControls
viewport/render.cljs      — Render loop + scene update (clear Group → rebuild meshes)
viewport/mesh_builder.cljs — BufferGeometry/Mesh construction
```

A Reagent layer panel (`ui/layer_panel.cljs`) is defined but unmounted. The scene manager (`scene/manager.cljs`) owns a `scene` atom and calls `update-viewport!` on change, which tears down and rebuilds all Three.js objects.

The `three-cad-viewer` library (bernhard-42/three-cad-viewer v4.3.9) provides a complete CAD viewer component: Viewer, Display (DOM layout), controls (trackball/orbit/Z-up), tree panel, toolbar, clipping planes, measurement, SSAO, material editing — all built on Three.js 0.184.0. The Shape data format matches our tessellation output nearly 1:1.

## Goals / Non-Goals

**Goals:**
- Replace manual Three.js viewport layer with `three-cad-viewer` library's Viewer + Display
- Maintain the dual-state pattern: scene atom for bookkeeping, Viewer API for rendering
- Map tessellation data to the library's full Shape format (all 9 fields)
- Map tagged sub-geometry (the `tag` macro) to hierarchical tree children
- Sync the library's tree panel state changes back to the scene atom
- Keep the `defmodel` macro, reactive params watcher, and model registry entirely unchanged
- Keep the OCCT kernel initialization and tessellation pipeline unchanged in structure

**Non-Goals:**
- Using the library's material editing, measurement, or clipping plane APIs (available but not wired in this change)
- Custom toolbar buttons or theme development
- Replacing the scene manager's reactivity model — our `params` atom watcher remains
- TypeScript conversion or React integration

## Decisions

### 1. Dual-state scene management

The scene manager maintains both the `scene` atom (bookkeeping: what's evaluated, params, visibility) and calls Viewer API methods (`addPart`, `updatePart`, `removePart`) as the rendering side-effect. This keeps the reactive model evaluator testable without a viewer and avoids baking library API calls into the model evaluation path.

```
Reactive flow:
  params atom change
    → watcher identifies dirty models
    → re-evaluates model functions
    → tessellates new shapes
    → updates scene atom (our state)
    → calls viewer.updatePart(path, shapeData)
         → library handles GPU buffer reuse or fallback

UI flow:
  user toggles visibility in library tree panel
    → library calls notify callback
    → callback updates scene atom visibility
```

### 2. File elimination pattern

| File | Status | Rationale |
|------|--------|-----------|
| `viewport/scene.cljs` | Remove | Display+Viewer owns scene/camera/renderer |
| `viewport/controls.cljs` | Remove | Library provides trackball + orbit + presets |
| `viewport/render.cljs` | Remove | Library owns render loop via requestAnimationFrame |
| `viewport/mesh_builder.cljs` | Remove | Data adapter in the scene manager calls Viewer API directly |
| `ui/layer_panel.cljs` | Remove | Library tree panel replaces it |

### 3. Shape adapter layer

The tessellation output maps to the library's Shape format with a simple transformation:

```clojure
(defn tessellation→shape [{:keys [vertices normals indices edges
                                   obj-vertices face-types edge-types
                                   triangles-per-face segments-per-edge]}]
  #js {:vertices     (clj->js vertices)
       :triangles    (clj->js indices)       ;; renamed key
       :normals      (clj->js normals)
       :edges        (clj->js edges)
       :obj_vertices (clj->js obj-vertices)
       :face_types   (clj->js face-types)
       :edge_types   (clj->js edge-types)
       :triangles_per_face  (clj->js triangles-per-face)
       :segments_per_edge   (clj->js segments-per-edge)})
```

Each model becomes a library Part with:
- `id: "/<model-name>"`
- `type: "shapes"`, `subtype: "solid"`
- Per-part `color` and `alpha` derived from model display options
- `state: [1, 1]` (shape visible, edges visible) by default

Tagged sub-geometry becomes child Parts:
- `id: "/<model-name>/<tag-label>"`
- Added via `viewer.addPart("/<model-name>", childPart)`

### 4. Kernel metadata expansion

The existing `extract-faces` function already iterates each face via `TopExp_Explorer(TopAbs_FACE)` and has access to `BRep_Tool.Triangulation`. We add:

- **`face_types`**: Call `BRepAdaptor_Surface` on each face to get `GeomAbs_SurfaceType` (0=Plane, 1=Cylinder, 2=Cone, 3=Sphere, ...)
- **`triangles_per_face`**: Count triangles extracted per face iteration
- **`obj_vertices`**: Collect unique vertices per face via `Poly_Triangulation.get().Node(i)` without transformation deduplication

The existing `extract-edges` function already iterates via `TopExp_Explorer(TopAbs_EDGE)` and has `BRepAdaptor_Curve`. We add:

- **`edge_types`**: Call `.GetCurveType()` on the adaptor (0=Line, 1=Circle, 2=Ellipse, ...)
- **`segments_per_edge`**: Count sample points per edge iteration

Memory management follows the existing pattern: OCCT objects created during extraction are explicitly `.delete()`-ed.

### 5. Viewer lifecycle

```clojure
(defn init-viewer! [container-id]
  (let [container (js/document.getElementById container-id)
        display (Display. container
                  #js {:cadWidth (.-clientWidth container)
                       :height (.-clientHeight container)
                       :treeWidth 240
                       :theme "dark"})
        viewer (Viewer. display
                  #js {:target #js [0 0 0] :up "Z"}
                  notify-callback)]
    ;; Store for use by scene manager
    (reset! *viewer viewer)
    (reset! *display display)
    viewer))
```

The library CSS is loaded via `<link>` in `index.html`. The `#viewport` div becomes the container for `Display`.

### 6. Notification callback for state sync

The library's Viewer constructor accepts a callback: `function notify(change)`. The change payload contains part paths and their new visibility state. We parse this and update the scene atom accordingly:

```clojure
(defn notify-callback [change]
  (when-let [path (.-id change)]
    ;; path = "/ModelName" or "/ModelName/tag-label"
    ;; state = [shape-visibility, edges-visibility]
    (update-scene-atom-from-path! path (.-state change))))
```

This ensures that toggling visibility in the library tree panel is reflected in our scene atom, keeping the dual state consistent.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| **Three.js version jump 0.160 → 0.184** | Our code no longer imports Three.js directly — the library consumes it. Test that shadow-cljs resolves the transitive dependency correctly. |
| **Shadow-cljs ESM module resolution** | The library ships ESM + UMD. If shadow-cljs cannot `require()` the ESM bundle, fall back to the UMD bundle path or add a bridge entry in `js/external-index.js`. |
| **Library CSS conflicts** | The library's CSS is scoped under `.tcv-*` classes. Low risk of conflict with our minimal existing styles. |
| **Library's notify callback schema undocumented** | Infer from TypeScript types shipped with the library; test with a simple console.log callback in the REPL during development. |
| **Per-face metadata computation adds tessellation overhead** | For PoC primitives (sphere = 1 face type, box = 6 face types), the overhead is negligible. Monitor if complex shapes with hundreds of faces cause perceptible delay. |
| **Removal of `patch-three.js` breaks existing Three.js resolution** | Three.js 0.184.0 is now a transitive dependency of three-cad-viewer. The library bundles its own Three.js import — shadow-cljs should not need to resolve Three.js directly. Verify with a clean `npm install`. |

## Open Questions

- Does `three-cad-viewer`'s ESM bundle work with shadow-cljs's `:require` out of the box, or does it need the UMD fallback?
- Does the library's `Shape` format accept missing optional metadata fields gracefully for the initial integration step?
- What is the exact payload schema of the notify callback? (Infer from TypeScript types or test empirically.)
