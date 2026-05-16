(ns clojcad.runner
  (:require
   [cljs.test :refer [run-tests]]
   [ClojCAD.kernel.lifecycle-test]
   [ClojCAD.model.registry-test]
   [ClojCAD.model.tag-test]
   [ClojCAD.model.core-test]
   [ClojCAD.scene.manager-test]
   [ClojCAD.viewport.shape-adapter-test]
   [ClojCAD.viewport.config-test]))

(defn -main [& args]
  (run-tests
   'ClojCAD.kernel.lifecycle-test
   'ClojCAD.model.registry-test
   'ClojCAD.model.tag-test
   'ClojCAD.model.core-test
   'ClojCAD.scene.manager-test
   'ClojCAD.viewport.shape-adapter-test
   'ClojCAD.viewport.config-test))
