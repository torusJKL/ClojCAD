(ns CADscript.core
  (:require [CADscript.kernel.api]
            [CADscript.model.core]
            [CADscript.model.registry]
            [CADscript.model.tag]
            [CADscript.scene.manager]))

(defn init []
  (println "CAD REPL PoC initialized"))
