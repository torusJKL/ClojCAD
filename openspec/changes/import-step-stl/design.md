## Context

ClojCAD provides a parametric modeling DSL in the browser using OCCT's B-Rep kernel via opencascade.js (zalo fork, `cascadestudio-v2` branch). Export to STL and STEP is already implemented and working. Import is the natural complement — users need to load existing 3D models from STEP (CAD interchange) and STL (3D printing/mesh) formats.

The opencascade.js fork already includes all OCCT reader classes needed (`STEPControl_Reader_1`, `StlAPI_Reader`, `BRepBuilderAPI_MakeSolid`), confirmed present in the existing `cascadestudio.d.ts` and WASM dist. No fork rebuild is needed.

CascadeStudio's `CascadeStudioFileIO` (same opencascade.js fork) demonstrates working STEP and STL import. Its code reveals the exact OCCT JS API patterns for file reading, shape construction, and MEMFS management.

## Goals / Non-Goals

**Goals:**
- Create `ClojCAD.kernel.import` namespace with `import-stl` and `import-step` functions
- Support both ASCII and binary STL files (detect format; `StlAPI_Reader` handles both)
- Support STEP AP203 and AP214 (both handled by `STEPControl_Reader_1`)
- Expose both import functions in `ClojCAD.kernel.api`
- Implement file upload via browser `<input type="file">` element
- Add imported shapes to the scene for display alongside parametric models
- Handle error cases: invalid/corrupt files, unsupported formats, reader failures
- Manage OCCT reader object lifecycle (create, use, delete) consistent with existing patterns
- Add an "Import" button to the three-cad-viewer toolbar with file picker

**Non-Goals:**
- IGES import (out of scope — only STEP and STL)
- Batch/multi-file import (single file picker for V1)
- Imported shape editing (read-only display for V1)
- Boolean operations on imported shapes (they may be shells, not solids)
- Import resolution/density tuning (use OCCT reader defaults)

## Decisions

### 1. STL format detection: ASCII vs binary
**Decision**: `StlAPI_Reader` handles both formats natively — no conversion needed.

OCCT's `StlAPI_Reader::Read()` accepts both ASCII and binary STL files. The reader inspects the file header. If the file starts with `solid` it's ASCII; otherwise binary. The reader decides internally which parser to use. No pre-processing or format conversion is needed.

However, there is a key detail: for the MEMFS write, binary files need `oc.FS.createDataFile()` with binary data rather than text. We will:
- Read the uploaded file as `ArrayBuffer` via `FileReader.readAsArrayBuffer()`
- Write to MEMFS as a binary file
- Pass the MEMFS path to `StlAPI_Reader.Read()`

### 2. File upload mechanism
**Decision**: Use a hidden `<input type="file">` element triggered by a toolbar button, reading files via `FileReader`.

Since ClojCAD runs in the browser:
1. Create a hidden `<input type="file" accept=".stl,.step,.stp">` element
2. On "Import" button click, programmatically click the file input
3. On file selection, read via `FileReader`:
   - For STL: `readAsArrayBuffer` (to handle binary)
   - For STEP: `readAsText` (STEP is always text)
4. After reading, call the appropriate kernel import function

### 3. MEMFS file handling for binary STL
**Decision**: Write binary STL data to MEMFS using `oc.FS.createDataFile()` with `encoding` omitted (raw bytes).

`oc.FS.createDataFile(parent, name, data, canRead, canWrite)` accepts a `Uint8Array` for binary data. We'll:
1. Wrap the uploaded `ArrayBuffer` in a `Uint8Array`
2. Call `oc.FS.createDataFile("/", tempFileName, uint8Array, true, true)`
3. Use `StlAPI_Reader` to read from MEMFS
4. Clean up with `oc.FS.unlink()`

For STEP (always ASCII text), we write with string data (which MEMFS treats as UTF-8).

### 4. STL shape conversion: shell to solid
**Decision**: Use `TopoDS_Cast.Shell_1` + `BRepBuilderAPI_MakeSolid_1` to produce a solid, mirroring CascadeStudio's approach.

`TopoDS_Cast` IS available in the WASM at runtime — it's registered in the opencascade.js fork's `additionalBindCode` via `EMSCRIPTEN_BINDINGS` (as a C++ wrapper for OCCT's `TopoDS::Shell()` etc.). It's absent from `.d.ts` only because custom Embind bindings are not auto-generated into TypeScript declarations. The class is compiled into the WASM and callable from JavaScript.

Full conversion pipeline:
1. `reader.Read(readShape, memfsPath)` — populates `readShape` (a `TopoDS_Shape` wrapping a `Shell`)
2. `(.TopoDS_Cast oc) (.Shell_1 readShape)` — downcasts to `TopoDS_Shell` (required by `BRepBuilderAPI_MakeSolid.Add()`)
3. `(js/Reflect.construct (.-BRepBuilderAPI_MakeSolid_1 oc) #js [])` — create solid builder
4. `(.Add solidStl shell)` — feed shell into builder
5. `(.Solid solidStl)` — extract `TopoDS_Solid`

This produces a proper OCCT Solid, enabling boolean operations on imported STL shapes.

### 5. STEP import pattern
**Decision**: Follow CascadeStudio's proven STEP import API.

Using the same pattern as CascadeStudio's `importSTEPorIGES`:
- Constructor: `new oc.STEPControl_Reader_1()` (numbered overload for default constructor)
- Read: `.ReadFile(filename)` — reads from MEMFS root, returns `IFSelect_ReturnStatus`
- Transfer: `.TransferRoots(new oc.Message_ProgressRange_1())` — converts to OCCT shapes
- Result: `.OneShape()` — returns the root `TopoDS_Shape`
- Cleanup: `oc.FS.unlink("/" + filename)` — delete temp file from MEMFS
- Status check: Compare against `oc.IFSelect_ReturnStatus.IFSelect_RetDone`

### 6. Memory management for reader objects
**Decision**: Track reader objects with `oc.delete()` immediately after use.

Reader objects are single-use — they hold internal data structures only needed during the read. We create, use, and delete them within the import function body. This is consistent with how writers are handled in `export.cljs`.

### 7. Import button / file picker UI
**Decision**: Add a hidden file input triggered by a toolbar button, styled to match the existing export button.

The import button will:
1. Look identical to the export button (same CSS classes `tcv_btn tcv_btn_highlight tcv_round`)
2. Use a file upload (up arrow) icon SVG
3. On click, trigger the hidden `<input type="file">`

The file input accepts `.stl`, `.step`, `.stp` extensions. On file selection, the handler reads the file and calls the appropriate kernel import function, then adds the shape to the scene.

### 8. Adding imported shapes to the scene
**Decision**: Imported shapes register via the scene manager's existing display mechanism.

After importing, the shape is added to the scene via the scene manager's `show` function. This makes imported shapes appear in the viewer and tree panel alongside parametric models. The model name is derived from the filename.

### 9. Loading indicator during import/export
**Decision**: Show a full-page semi-transparent overlay with a CSS border spinner during import and export operations.

Import is async (FileReader promise) — the loading overlay shows immediately on file selection and hides when the promise chain completes. Export is synchronous (OCCT work + browser download trigger) — `setTimeout(fn, 50)` is used to defer the operation to the next paint cycle, giving the browser time to render the overlay before the synchronous work begins. Without this deferral, the overlay would be added and removed within the same frame, never visible to the user.

### 10. User-facing error notifications
**Decision**: Show a red toast notification at the bottom of the viewport when an import or export operation fails, rather than only logging to the console.

Failures are silent to non-developer users if only logged to console. A toast provides immediate visible feedback. The toast auto-dismisses after 4 seconds — long enough to read, short enough not to block interaction. Styling uses inline DOM (no CSS dependencies) with a red background matching error conventions.

Cases covered:
- Import: corrupt/invalid file, unsupported file type, file read error
- Export: no model displayed, no shape data for current model

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| Binary STL read/write to MEMFS may have encoding issues | Test with known binary STL files. Use `Uint8Array` for MEMFS writes — OCCT's `StlAPI_Reader` handles both formats. |
| Large STL files cause WASM heap OOM | Same risk as existing boolean/tessellation operations. Files >50MB are unusual for STL; warn if detected. |
| `TopoDS_Cast.Shell_1()` may throw if the imported shape isn't a Shell (e.g. compound) | Guard with try/catch. If it fails, fall back to using the shape directly (rendering still works even if booleans won't). |
| `StlAPI_Reader` may return compound shapes with multiple shells | The shape is still renderable via tessellation. Each shell is a separate connected component in the mesh. |
| Browser file size limits for `FileReader` | `FileReader.readAsArrayBuffer` handles files up to ~500MB in modern browsers. Real STL/STEP files rarely exceed 100MB. |
