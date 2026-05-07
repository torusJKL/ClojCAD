(ns CADscript.kernel.init)

(defonce oc-instance (atom nil))
(defonce loading? (atom true))
(defonce error (atom nil))

(defn init-kernel []
  (let [dynamic-import (.call (js/Function "return import('/opencascade.js')") js/undefined)]
    (-> dynamic-import
        (.then (fn [mod]
                 (let [opencascade (.-default mod)]
                   (-> (opencascade.
                        #js {:locateFile
                             (fn [path]
                               (if (.endsWith path ".wasm")
                                 "/opencascade.wasm.wasm"
                                 path))})
                       (.then (fn [oc]
                                  (reset! oc-instance oc)
                                  (reset! loading? false)
                                  oc))
                       (.catch (fn [e]
                                 (reset! error (str e))
                                 (reset! loading? false)
                                 nil))))))
        (.catch (fn [e]
                  (reset! error (str "Import failed: " e))
                  (reset! loading? false)
                  nil)))))
