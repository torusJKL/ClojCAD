(ns CADscript.core
  (:require [CADscript.kernel.api]
            [CADscript.model.core]
            [CADscript.model.registry]
            [CADscript.model.tag]
            [CADscript.scene.manager :as sm]
            [CADscript.viewport.scene :as vs]
            [CADscript.viewport.controls :as vc]
            [CADscript.viewport.render :as vr]))

(defn init []
  (println "CAD REPL PoC initialized")
  (vs/init-viewport!)
  (vc/init-controls!)
  (sm/set-on-update! vr/update-viewport!)
  (vr/start-loop!))
