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
                                 (js/console.log "OC module loaded" (.-BufferGeometry oc))
                                 (try
                                   (js/console.log "BRepPrimAPI_MakeSphere:" (str oc.BRepPrimAPI_MakeSphere))
                                   (catch :default e
                                     (js/console.log "MakeSphere error:" (str e))))
                                 (try
                                   (js/console.log "BRepPrimAPI_MakeBox:" (str oc.BRepPrimAPI_MakeBox))
                                   (catch :default e
                                     (js/console.log "MakeBox error:" (str e))))
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
