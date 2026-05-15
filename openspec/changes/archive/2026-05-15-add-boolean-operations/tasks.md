## 1. Boolean Kernel Module

- [x] 1.1 Create `src/ClojCAD/kernel/booleans.cljs` with namespace `ClojCAD.kernel.booleans`, require `init` and `lifecycle`
- [x] 1.2 Implement private `-fuse-2` binary helper wrapping `BRepAlgoAPI_Fuse`: construct builder, extract `.Shape()`, delete builder, track result, return nil on null/IsNull
- [x] 1.3 Implement private `-common-2` binary helper wrapping `BRepAlgoAPI_Common` following same pattern
- [x] 1.4 Implement private `-cut-2` binary helper wrapping `BRepAlgoAPI_Cut` following same pattern
- [x] 1.5 Implement public `fuse` with variadic signature `(a b & more)`: binary reduce on the two-arg helper, short-circuit on nil
- [x] 1.6 Implement public `common` with variadic signature `(a b & more)` following same reduce pattern
- [x] 1.7 Implement public `cut` with variadic signature `(a b & more)` following same reduce pattern
- [x] 1.8 Wrap each binary helper body in try/catch, log warning via `js/console.warn` on failure, return nil

## 2. Public API Wiring

- [x] 2.1 Add `:require [ClojCAD.kernel.booleans :as booleans]` to `src/ClojCAD/kernel/api.cljs`
- [x] 2.2 Export `def fuse` `def common` `def cut` from `kernel/api.cljs`

## 3. Demo and Verification

- [x] 3.1 Add `:require [ClojCAD.kernel.booleans :as booleans]` to `src/ClojCAD/demo.cljs`
- [x] 3.2 Add `defmodel with-hole` demo in `demo.cljs` using `booleans/cut` (box minus cylinder)
- [x] 3.3 Add `defmodel intersected` demo in `demo.cljs` using `booleans/common` (sphere intersected with box)
- [x] 3.4 Add `defmodel combined` demo in `demo.cljs` using `booleans/fuse` (two spheres fused)
- [x] 3.5 Update `start-demo!` to `sm/show` the boolean demo models
- [x] 3.6 Run `shadow-cljs watch dev` and verify all three boolean demos render correctly in browser
