(ns CADscript.kernel.init)

(defonce oc-instance (atom nil))
(defonce loading? (atom true))
(defonce error (atom nil))

(defn init-kernel []
  (-> (js/import "opencascade.js/dist/opencascade.wasm.js")
      (.then (fn [mod]
               (let [opencascade (.-default mod)
                     oc (opencascade.)]
                 (reset! oc-instance oc)
                 (reset! loading? false)
                 oc)))
      (.catch (fn [e]
                (reset! error (str e))
                (reset! loading? false)
                nil))))
