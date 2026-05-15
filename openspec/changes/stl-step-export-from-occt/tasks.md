## 1. (confirmed) Export OCCT classes are already in the WASM dist

Confirmed present in `cascadestudio.d.ts`: `StlAPI_Writer`, `STEPControl_Writer_1`, `STEPControl_StepModelType`, `IFSelect_ReturnStatus`, `Message_ProgressRange_1`. No fork rebuild needed.

## 2. Implement ClojureScript export module

- [x] 2.1 Create `src/ClojCAD/kernel/export.cljs`
- [x] 2.2 Add `read-memfs!` helper using `oc.FS.readFile`
- [x] 2.3 Add `download-blob!` helper (Blob + URL.createObjectURL + anchor click)
- [x] 2.4 Implement `export-stl` using `StlAPI_Writer`
- [x] 2.5 Implement `export-step` using `STEPControl_Writer_1` + `Transfer_1` + `IFSelect_RetDone`
- [x] 2.6 Multi-shape STEP export (vector of shapes)
- [x] 2.7 Error handling (try/catch, null checks, transfer status checking)

## 3. Wire export into kernel API

- [x] 3.1 Require export ns in `api.cljs`
- [x] 3.2 Add `export-stl` and `export-step` defs to public API

## 4. Build export dropdown UI

- [x] 4.1 Create `src/ClojCAD/viewport/export_ui.cljs`
- [x] 4.2 Implement `mount-export-button!` with toolbar-styled button
- [x] 4.3 Implement dropdown popup with STL/STEP options
- [x] 4.4 Wire dropdown items to kernel export functions
- [x] 4.5 Auto-generate filename from model name
- [x] 4.6 Mount export button from `viewer.cljs` after `init-viewer!`

## 5. Test and verify

- [x] 5.1 App loads without WASM errors (confirmed during development)
- [x] 5.2 STL export from REPL works (confirmed working)
- [x] 5.3 STEP export from REPL works (confirmed working)
- [x] 5.4 Export after boolean operations works (confirmed working)
- [x] 5.5 Error handling: nil shape produces warning without crash (confirmed)
- [x] 5.6 OCCT heap: repeated exports succeed without WASM heap exhaustion (confirmed)
- [x] 5.7 Export dropdown button appears in toolbar and toggles correctly (confirmed)
- [x] 5.8 Export from dropdown triggers correct format download (confirmed)
