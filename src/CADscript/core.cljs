(ns CADscript.core
  (:require [CADscript.model.core]
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
  (vs/init-viewport!)
  (vc/init-controls!)
  (sm/set-on-update! vr/update-viewport!)
  (vr/start-loop!)
  (demo/start-demo!)
  (rdomc/render ui-root [lp/layer-panel]))
