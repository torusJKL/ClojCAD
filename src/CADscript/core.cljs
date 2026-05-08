(ns CADscript.core
  (:require [CADscript.model.core]
            [CADscript.model.registry]
            [CADscript.model.tag]
            [CADscript.scene.manager :as sm]
            [CADscript.viewport.scene :as vs]
            [CADscript.viewport.controls :as vc]
            [CADscript.viewport.render :as vr]
            [CADscript.kernel.api :as kernel]
            [CADscript.demo :as demo]))

(defn init []
  (vs/init-viewport!)
  (vc/init-controls!)
  (sm/set-on-update! vr/update-viewport!)
  (vr/start-loop!)
  (-> (kernel/init-kernel)
      (.then (fn [_]
               (demo/start-demo!)))
      (.catch (fn [e]
                (js/console.error "init-kernel failed:" e)))))
