(ns ClojCAD.core
  (:require [ClojCAD.model.core]
            [ClojCAD.model.registry]
            [ClojCAD.model.tag]
            [ClojCAD.scene.manager :as sm]
            [ClojCAD.viewport.viewer :as vw]
            [ClojCAD.kernel.api :as kernel]
            [ClojCAD.viewport.config :as cfg]
            [ClojCAD.demo :as demo]))

(defn init
  "Main entry point for ClojCAD. Initializes the viewer, loads config, starts the
   OpenCASCADE kernel, then launches the demo." []
  (vw/init-viewer!)
  (-> (cfg/load-config!)
      (.then (fn [_]
               (kernel/init-kernel)))
      (.then (fn [_]
               (demo/start-demo!)))
      (.catch (fn [e]
                (js/console.error "init failed:" e)))))
