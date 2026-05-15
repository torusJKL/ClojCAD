## Context

The three-cad-viewer library provides edge rendering via `LineSegmentsGeometry` (Three.js fat lines). It expects edge data as flat `Float32Array` where every 6 consecutive floats (2 × 3D points) form one line segment. The OCCT `extract-edges` function samples points along each topological edge but outputs raw sample positions — these need to be restructured into connected segment pairs.

The viewer also supports a `blackEdges` toggle that swaps edge color to `0x000000`, and a `setBlackEdges(flag)` API method. Currently the `blackEdges` parameter isn't passed through the ClojureScript layer, and edge data format may be incorrect for curved edges.

## Goals / Non-Goals

**Goals:**
- Restructure edge tessellation output so every consecutive pair of sample points becomes a connected line segment
- Surface edges correctly through both initial render (`Viewer.render`) and dynamic update (`Viewer.updatePart`, `Viewer.addPart`) paths
- Ensure the "Show black edges" toolbar toggle has a visible effect (edges appear and toggle correctly)

**Non-Goals:**
- Changing the edge color to anything other than what three-cad-viewer manages via its toolbar (`edgeColor` vs `blackEdges`)
- Adding new UI controls beyond what three-cad-viewer already provides in the toolbar/tree

## Decisions

1. **Fix edge segment format in `extract-edges`** — Instead of outputting raw sample points, iterate pairwise (`p_i, p_{i+1}`) and push 6 values per segment. This ensures `LineSegmentsGeometry.setPositions` receives correctly paired data regardless of how many sample points an edge has.

2. **Keep `shape_adapter.cljs` pass-through** — The adapter already forwards edges as-is, which is correct once the format is fixed. No changes needed.

3. **No `blackEdges` option in init** — The `blackEdges` toggle is managed by the library's built-in toolbar button which calls `Viewer.setBlackEdges()`. The initial state (`false`) matches the default. No explicit ClojureScript wiring is needed — the button's handler already targets the viewer instance.

4. **Verify all render paths** — The three-cad-viewer `render()`, `addPart()`, and `updatePart()` methods all delegate to `NestedGroup.renderShape()` which checks `shape.edges` and renders if present. Once format is fixed, all paths should render edges correctly.

## Risks / Trade-offs

- **Risk: Missing segment at curve endpoints** → Confirmed: currently curved edges (N>2) produce disconnected or orphaned segments. Fixing the format resolves this.
- **Risk: Performance on dense tessellation** → More sample points = more segments. Each edge already passes through `GCPnts_TangentialDeflection` with a controlled deviation, so sample count is bounded. Acceptable.
- **Risk: Camera preset "iso" obscures thin edges** → Edge lines have `renderOrder: 999` so they render on top of faces. This is handled by the library, not our concern.
