## Context

ClojCAD provides a parametric modeling DSL in the browser using OCCT's B-Rep kernel via opencascade.js (zalo fork, `cascadestudio-v2` branch). Currently, shapes exist only in memory and are rendered via tessellation to the three-cad-viewer. There is no mechanism to persist shapes to standard interchange formats.

The opencascade.js fork uses a custom build configuration (`builds/cascadestudio.yml`) that selects ~120 OCCT classes for binding. The `StlAPI_Writer` and `STEPControl_Writer` classes are not currently in that configuration. The fork must be rebuilt with additional bindings, or a prebuilt WASM must be provided.

Export runs entirely client-side — the OCCT WASM does the conversion, and the result is downloaded as a file via the browser's Blob/download API.

## Goals / Non-Goals

**Goals:**
- Create `ClojCAD.kernel.export` namespace with `export-stl` and `export-step` functions
- Expose both functions in `ClojCAD.kernel.api`
- Implement browser file download via Blob + dynamic anchor click
- Handle error cases: invalid/null shape, write failure, unsupported shape type
- Manage OCCT writer object lifecycle (tracking + deletion) consistent with existing patterns
- Add an "Export" dropdown button to the three-cad-viewer toolbar for one-click export of the displayed shape

**Non-Goals:**
- STL ASCII format (binary only — smaller, faster, standard for 3D printing)
- STEP import (IGES import/export is out of scope)
- Batch/multi-file export
- Asynchronous export (export is synchronous in OCCT WASM)
- Custom export options (density, tolerance, schema) — use OCCT defaults, configurable later

## Decisions

### 1. Rebuild opencascade.js fork vs manual STL writer
**Decision**: Rebuild the fork with new bindings.

Both `StlAPI_Writer` and `STEPControl_Writer` are part of OCCT's standard library. Rebuilding the fork with these classes gives us:
- Proper tessellation control for STL (OCCT's `StlAPI_Writer` applies `BRepMesh_IncrementalMesh` internally)
- Correct STEP AP203/AP414 header and structure guaranteed by OCCT
- Future-proofing for IGES import/export and other OCCT I/O classes

Manual STL from mesh data would be simpler but loses the "from original OCCT objects" requirement — the user wants B-Rep level export, not tessellation dump.

### 2. STEP schema: AP203 over AP214
**Decision**: Default to AP203 (ISO 10303-203 "Configuration Controlled 3D Design of Mechanical Parts and Assemblies").

AP203 is the most widely supported STEP schema in CAD/CAM tools. AP214 adds automotive-specific constructs that most tools don't use. `STEPControl_Writer` supports both via `STEPControl_StepModelType` enum (`STEPControl_AsIs` = AP203).

### 3. STL format: binary over ASCII
**Decision**: Binary STL only.

Binary STL is ~5x smaller than ASCII, faster to write, and universally supported by slicers and 3D printing tools. `StlAPI_Writer::Write()` defaults to binary with `.SetASCIIMode(Standard_False)`.

### 4. File download via Blob + anchor click
**Decision**: Use the browser's Blob API + dynamically created anchor element with download attribute.

Since ClojCAD runs in the browser (not Node.js), there is no filesystem. The standard browser pattern is:
1. Create a `Blob` from the OCCT output data
2. Create an object URL via `URL.createObjectURL(blob)`
3. Create a hidden `<a>` element with the URL and `download` attribute
4. Programmatically click the anchor
5. Revoke the object URL

### 5. Follow CascadeStudio's proven OCCT export API
**Decision**: Mirror CascadeStudio's `CascadeStudioFileIO.saveShapeSTEP` implementation exactly.

CascadeStudio uses the same `opencascade.js` fork (`cascadestudio-v2`) and has working STEP export in production. Its code reveals the exact OCCT JS API patterns:

- **Constructor**: `new oc.STEPControl_Writer_1()` (numbered overload for default constructor)
- **Transfer**: `.Transfer_1(shape, STEPControl_StepModelType.STEPControl_AsIs, true, new oc.Message_ProgressRange_1())` — 4 args, returns `IFSelect_ReturnStatus`
- **Write**: `.Write(filename)` — writes to MEMFS root (`/`), returns `IFSelect_ReturnStatus`
- **Read**: `oc.FS.readFile("/" + filename, {encoding: "utf8"})` — STEP is UTF-8 text
- **Cleanup**: `oc.FS.unlink("/" + filename)` — delete temp file from MEMFS
- **Status check**: Compare against `oc.IFSelect_ReturnStatus.IFSelect_RetDone`

For STL export, `StlAPI_Writer.Write(shape, path)` is a simpler call (returns boolean), and the file is read as binary via `oc.FS.readFile(path)`.

This also confirms:
- File path is `"/" + filename` (not `/tmp/` — MEMFS root)
- `StlAPI_Reader` is available on the fork (confirmed by CascadeStudio's STL import)
- No `Interface_Static`, `Transfer_FinderProcess`, or other auxiliary classes need explicit binding — they're pulled in transitively

### 6. Memory management for writer objects
**Decision**: Track writer objects with the existing lifecycle mechanism, but delete writers immediately after use rather than keeping them until `destroy-all`.

Writer objects (`StlAPI_Writer`, `STEPControl_Writer`) are single-use — they hold internal data structures only needed during the write call. We allocate, use, and delete them within the export function body. This avoids leaking WASM heap memory and is consistent with how `BRepAlgoAPI_*` builders are handled in booleans.cljs.

### 6. Dropdown button approach
**Decision**: Create a custom dropdown button widget using raw DOM, appended to the toolbar div after viewer initialization.

The three-cad-viewer's `Button` and `ClickButton` classes are not exported from the module, so we can't use `display.cadTool.addButton()` from ClojureScript. Instead, we build the button DOM directly and append it to `display.cadTool.container` (the toolbar's root `<div>`).

The button uses the same CSS classes as native toolbar buttons (`.tcv_btn`, `.tcv_btn_highlight`, `.tcv_round`) so it blends in visually. The dropdown is a simple absolutely-positioned popup with two items ("Export STL (.stl)" and "Export STEP (.step)"), styled to match the toolbar's dark theme.

The export action reads the currently displayed shape from the model registry or scene manager, and calls the corresponding kernel export function with an auto-generated filename based on the model name.

Implementation in `src/ClojCAD/viewport/export_ui.cljs`:
- `mount-export-button! [display]` — creates the button + dropdown DOM, appends to toolbar, wires click handlers
- Called once from `viewer.cljs` after `init-viewer!` sets up the display

### 7. OCCT export classes already in the existing WASM dist
**Confirmed**: All OCCT classes needed for export are already present in the current `cascadestudio.js` / `.wasm` dist. No fork rebuild needed.

Found at the end of the existing `cascadestudio.d.ts` (4186 lines):

- `StlAPI_Writer` — writes STL. Exposed as `StlAPI_Writer` (no numbered constructors needed: `Write` is a direct method).
- `STEPControl_Writer_1` — default constructor for the STEP writer. Also has `STEPControl_Writer_2`.
- `STEPControl_StepModelType` — enum with `STEPControl_AsIs` (maps to AP203).
- `IFSelect_ReturnStatus` — enum with `IFSelect_RetDone` (value 0) for status checking.
- `Message_ProgressRange_1` — already bound, used as 4th arg to `Transfer_1`.

The fork's `builds/cascadestudio.yml` confirms these are in the build config under `# File I/O`:
```yaml
- symbol: STEPControl_Writer
- symbol: STEPControl_StepModelType
- symbol: StlAPI_Writer
- symbol: IFSelect_ReturnStatus
```

No additional classes need to be added. The WASM does not need to be rebuilt.

## Risks / Trade-offs

| Risk | Mitigation |
|------|------------|
| OCCT StlAPI_Writer tessellates at fixed precision | `StlAPI_Writer` uses `BRepMesh_IncrementalMesh` internally with OCCT default deflection. If users need mesh control, expose `maxDeviation` parameter in `export-stl`. |
| Browser download API varies across browsers | Blob + anchor pattern is supported in all modern browsers (Chrome, Firefox, Safari, Edge). |
| Large shapes cause WASM heap OOM | Same risk as existing boolean operations. If it becomes an issue, explore OCCT's incremental write or chunked output. |
