(ns CADscript.ui.layer-panel
  (:require [CADscript.scene.manager :as sm]))

(defn layer-panel []
  (let [entries (into [] (for [[name entry] @sm/scene]
                           [:div.layer-entry
                            (if-let [err (:error entry)]
                              [:div.error (str name ": " err)]
                              [:label
                               [:input {:type "checkbox"
                                        :checked (:visible? entry true)
                                        :on-change #(if (:visible? entry true)
                                                      (sm/hide name)
                                                      (sm/show-model name))}]
                               (str name)])]))]
    [:div.layer-panel
     [:h3 "Layers"]
     (into [:div] entries)]))
