(ns CADscript.ui.layer-panel
  (:require [CADscript.scene.manager :as sm]))

(defn layer-panel []
  (let [scene @sm/scene
        items (reduce-kv
                (fn [acc name entry]
                  (let [visible? (:visible? entry true)]
                    (conj acc
                      [:div {:key (str name) :class "layer-entry"}
                       (if-let [err (:error entry)]
                         [:div.error (str name ": " err)]
                         [:label
                          [:input {:type "checkbox"
                                   :checked visible?
                                   :on-change #(if visible?
                                                 (sm/hide name)
                                                 (sm/show-model name))}]
                          (str name)])])))
                [] scene)]
    [:div.layer-panel
     [:h3 "Layers"]
     (into [:div] items)]))
