## 1. Fix edge extraction in kernel/mesh.cljs

- [x] 1.1 Fix `.GetCurveType` → `.GetType` (wrong OCCT method name — was causing all edges to throw and be silently caught)
- [x] 1.2 Modify `extract-edges` to output connected segment pairs instead of raw sample points — iterate `p_i` to `p_{i+1}` and push 6 values per segment
- [x] 1.3 Verify the output: for N sample points, produce `(N-1) * 6` floats; for 2 points produce 6 floats (single segment)

## 2. Verify edge rendering through all viewer paths

- [x] 2.1 Run the dev server and observe that edges render on the boolean-bench demo shape
- [x] 2.2 Click the "Show black edges" toolbar toggle and confirm edges change color to black/grey

## 3. Build and verify

- [x] 3.1 Run `shadow-cljs` build and confirm no compilation errors
- [x] 3.2 Verify that the `tessellate` function output `:edges` is correctly formatted via `run!` or browser console inspection
