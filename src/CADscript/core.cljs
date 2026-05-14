(ns CADscript.core
  (:require [CADscript.model.core]
            [CADscript.model.registry]
            [CADscript.model.tag]
            [CADscript.scene.manager :as sm]
            [CADscript.viewport.viewer :as vw]
            [CADscript.kernel.api :as kernel]
            [CADscript.demo :as demo]))

(defn init []
  (vw/init-viewer!)
  (-> (kernel/init-kernel)
      (.then (fn [_]
               (demo/start-demo!)))
      (.catch (fn [e]
                (js/console.error "init-kernel failed:" e)))))
