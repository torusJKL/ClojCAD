(ns CADscript.kernel.init
  (:require ["opencascade.js" :as opencascade]))

(defonce oc-instance (atom nil))
(defonce loading? (atom true))
(defonce error (atom nil))

(defn init-kernel []
  (-> (opencascade)
      (.then (fn [oc]
               (reset! oc-instance oc)
               (reset! loading? false)
               oc))
      (.catch (fn [e]
                (reset! error (str e))
                (reset! loading? false)
                nil))))
