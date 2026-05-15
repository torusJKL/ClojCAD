## 1. Confirm OCCT import classes are in the WASM dist

- [x] 1.1 Verify `STEPControl_Reader_1`, `StlAPI_Reader`, `BRepBuilderAPI_MakeSolid_1` are present in `cascadestudio.d.ts`
- [x] 1.2 Verify `TopoDS_Cast` is accessible at runtime (registered via `additionalBindCode` — not in `.d.ts` but compiled into WASM)
- [x] 1.3 Verify `IFSelect_ReturnStatus` (for read status checking) is bound
- [x] 1.4 Verify `Message_ProgressRange_1` (for TransferRoots) is bound

## 2. Implement ClojureScript import module

- [x] 2.1 Create `src/ClojCAD/kernel/import.cljs` with `(ns ...)` requiring `ClojCAD.kernel.init`
- [x] 2.2 Implement `read-file-as-arraybuffer!` helper using `FileReader` API in import_ui.cljs (for binary STL)
- [x] 2.3 Implement `read-file-as-text!` helper using `FileReader` API in import_ui.cljs (for STEP)
- [x] 2.4 Implement `write-memfs-binary!` helper writing `Uint8Array` to MEMFS
- [x] 2.5 Implement `write-memfs-text!` helper writing text to MEMFS
- [x] 2.6 Implement `cleanup-memfs!` helper to unlink temp file
- [x] 2.7 Implement `import-stl` using `StlAPI_Reader` + `TopoDS_Cast.Shell_1` + `BRepBuilderAPI_MakeSolid_1` (handles both ASCII and binary)
- [x] 2.8 Implement `import-step` using `STEPControl_Reader_1` + `ReadFile` + `TransferRoots` + `OneShape`
- [x] 2.9 Error handling: try/catch around reader operations, null checks, MEMFS cleanup in finally

## 3. Wire import into kernel API

- [x] 3.1 Require import ns in `api.cljs`
- [x] 3.2 Add `import-stl` and `import-step` defs to public API

## 4. Build import file-picker UI

- [x] 4.1 Create `src/ClojCAD/viewport/import_ui.cljs`
- [x] 4.2 Create hidden `<input type="file" accept=".stl,.step,.stp">` element
- [x] 4.3 Create upload icon SVG button (matching export button style)
- [x] 4.4 Wire button click to trigger file input click
- [x] 4.5 Handle file selection: read file, detect format by extension, call kernel import
- [x] 4.6 Mount import button from `viewer.cljs` after `init-viewer!`

## 5. Wire imported shapes into scene

- [x] 5.1 After successful import, create scene model entry with filename as model name
- [x] 5.2 Tessellate imported shape via `kernel/tessellate`
- [x] 5.3 Push tessellated mesh to viewer via scene manager display pipeline

## 6. Add loading indicator and error notifications for import/export

- [x] 6.1 Create `src/ClojCAD/viewport/loading.cljs` with `show-loading!` / `hide-loading!` (full-page overlay with CSS spinner)
- [x] 6.2 Wire loading overlay into import: show on file selection, hide after shape renders or on error
- [x] 6.3 Wire loading overlay into export: show before export starts, hide after download (use setTimeout to allow paint before synchronous OCCT work)
- [x] 6.4 Add `notify!` function to loading.cljs (red toast at bottom of viewport, auto-dismiss after 4s)
- [x] 6.5 Wire notifications into import failures (corrupt file, unsupported type, read error)
- [x] 6.6 Wire notifications into export failures (no model, no shape data)

## 7. Test and verify

- [x] 7.1 App loads without WASM errors
- [x] 7.2 Import ASCII STL file from file picker (shape appears in viewer)
- [x] 7.3 Import binary STL file from file picker (shape appears in viewer)
- [x] 7.4 Import STEP file from file picker (shape appears in viewer)
- [x] 7.5 Import corrupt/invalid file produces warning and visible notification without crash
- [x] 7.6 Import button appears in toolbar and opens file picker on click
- [x] 7.7 Imported shapes display alongside parametric models
- [x] 7.8 Loading overlay appears during import and disappears on completion
- [x] 7.9 Loading overlay appears during export and disappears on completion
- [x] 7.10 Error notifications appear for import failures
- [x] 7.11 Error notifications appear for export failures
