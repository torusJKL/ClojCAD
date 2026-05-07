(ns CADscript.core
  (:require [CADscript.kernel.api :as kernel]
            [CADscript.model.core]
            [CADscript.model.registry]
            [CADscript.model.tag]
            [CADscript.scene.manager :as sm]
            [CADscript.viewport.scene :as vs]
            [CADscript.viewport.controls :as vc]
            [CADscript.viewport.render :as vr]
            [reagent.dom.client :as rdomc]
            [CADscript.ui.layer-panel :as lp]
            [CADscript.demo :as demo]))

(defonce ui-root (rdomc/create-root (js/document.getElementById "ui")))

(defn init []
  (println "CAD REPL PoC initialized")
  (vs/init-viewport!)
  (vc/init-controls!)
  (sm/set-on-update! vr/update-viewport!)
  (vr/start-loop!)
  (rdomc/render ui-root [lp/layer-panel])
  (demo/start-demo!)
  (-> (kernel/init-kernel)
      (.then (fn [oc]
               (when oc
                 (println "CAD kernel ready"))))))
