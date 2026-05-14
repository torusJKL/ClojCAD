(ns ClojCAD.core
  (:require [ClojCAD.model.core]
            [ClojCAD.model.registry]
            [ClojCAD.model.tag]
            [ClojCAD.scene.manager :as sm]
            [ClojCAD.viewport.viewer :as vw]
            [ClojCAD.kernel.api :as kernel]
            [ClojCAD.demo :as demo]))

(defn init []
  (vw/init-viewer!)
  (-> (kernel/init-kernel)
      (.then (fn [_]
               (demo/start-demo!)))
      (.catch (fn [e]
                (js/console.error "init-kernel failed:" e)))))
