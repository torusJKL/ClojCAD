(ns clojcad.runner
  (:require
   [cljs.test :refer [run-tests]]
   [ClojCAD.kernel.init :as init]
   ;; Non-WASM tests
   [ClojCAD.kernel.lifecycle-test]
   [ClojCAD.model.registry-test]
   [ClojCAD.model.tag-test]
   [ClojCAD.model.core-test]
   [ClojCAD.scene.manager-test]
   [ClojCAD.viewport.shape-adapter-test]
   [ClojCAD.viewport.config-test]
   [ClojCAD.viewport.loading-test]
   ;; WASM-dependent tests
   [ClojCAD.kernel.init-test]
   [ClojCAD.kernel.primitives-test]
   [ClojCAD.kernel.booleans-test]
   [ClojCAD.kernel.mesh-test]
   [ClojCAD.kernel.export-test]
   [ClojCAD.kernel.import-test]))

(defn- wasm-ready? []
  (some? @init/oc-instance))

(defn -main [& args]
  (println "--- Running non-WASM tests ---")
  (run-tests
   'ClojCAD.kernel.lifecycle-test
   'ClojCAD.model.registry-test
   'ClojCAD.model.tag-test
   'ClojCAD.model.core-test
   'ClojCAD.scene.manager-test
   'ClojCAD.viewport.shape-adapter-test
   'ClojCAD.viewport.config-test
   'ClojCAD.viewport.loading-test)
  (if (wasm-ready?)
    (do (println "--- WASM available, running kernel and export/import tests ---")
        (run-tests
         'ClojCAD.kernel.init-test
         'ClojCAD.kernel.primitives-test
         'ClojCAD.kernel.booleans-test
         'ClojCAD.kernel.mesh-test
         'ClojCAD.kernel.export-test
         'ClojCAD.kernel.import-test))
    (println "--- WASM not available, skipping kernel and export/import tests ---"
             "(call init/init-kernel before running if WASM is needed)")))
