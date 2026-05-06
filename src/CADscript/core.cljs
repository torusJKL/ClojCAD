(ns CADscript.core
  (:require [CADscript.kernel.api]
            [CADscript.model.core]
            [CADscript.model.registry]
            [CADscript.model.tag]))

(defn init []
  (println "CAD REPL PoC initialized"))
