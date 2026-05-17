(ns ClojCAD.kernel.init
  (:require ["opencascade.js" :default init-oc]
            [ClojCAD.kernel.font :as font]))

(defonce oc-instance (atom nil))
(defonce loading? (atom true))
(defonce error (atom nil))

(defn init-kernel
  "Initialize the OpenCASCADE WASM kernel and load bundled fonts.
   Returns a Promise that resolves when the kernel and fonts are ready.
   Check `loading?` or `error` atoms for status." []
  (-> (init-oc #js {:locateFile (fn [path]
                                  (if (.endsWith path ".wasm")
                                    "/cascadestudio.wasm"
                                    path))})
      (.then (fn [oc]
               (reset! oc-instance oc)
               (-> (font/load-bundled-fonts!)
                   (.then #(reset! loading? false))
                   (.catch (fn [e]
                     (js/console.warn "Font loading failed:" e)
                     (reset! loading? false))))))
      (.catch (fn [e]
                (reset! error (str e))
                (reset! loading? false)
                nil))))