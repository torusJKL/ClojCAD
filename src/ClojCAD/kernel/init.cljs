(ns ClojCAD.kernel.init
  (:require ["opencascade.js" :default init-oc]))

(defonce oc-instance (atom nil))
(defonce loading? (atom true))
(defonce error (atom nil))

(defn init-kernel []
  (-> (init-oc #js {:locateFile (fn [path]
                                  (if (.endsWith path ".wasm")
                                    "/cascadestudio.wasm"
                                    path))})
      (.then (fn [oc]
               (reset! oc-instance oc)
               (reset! loading? false)
               oc))
      (.catch (fn [e]
                (reset! error (str e))
                (reset! loading? false)
                nil))))