(ns CADscript.ui.layer-panel
  (:require [reagent.core :as r]
            [CADscript.scene.manager :as sm]
            [CADscript.kernel.init :as kernel]))

(defn layer-entry [[name entry]]
  (let [collapsed? (r/atom true)]
    (fn [[name entry]]
      [:div.layer-entry
       (if-let [err (:error entry)]
         [:div.error (str (str name) ": " err)]
         [:label
          [:input {:type "checkbox"
                   :checked (:visible? entry true)
                   :on-change #(if (:visible? entry true)
                                 (sm/hide name)
                                 (sm/show-model name))}]
          (str name)])
       (when (seq (:tags entry))
         [:div.sub-layers
          [:button.sub-toggle {:on-click #(swap! collapsed? not)}
           (if @collapsed? "▶" "▼")]
          (when-not @collapsed?
            (for [[label tag-data] (:tags entry)]
              ^{:key (str name "-" (name label))}
              [:div.sub-entry
               [:label
                [:input {:type "checkbox"
                         :checked (get-in entry [:tags-visible label] true)
                         :on-change #(if (get-in entry [:tags-visible label] true)
                                       (sm/hide-tag name label)
                                       (sm/show-tag name label))}]
                (str label)]]))])])))

(defn layer-panel []
  (let [loading? @kernel/loading?
        err @kernel/error]
    [:div.layer-panel
     [:h3 "Layers"]
     (into [:div]
           (for [[name entry] @sm/scene]
             ^{:key name} [layer-entry [name entry]]))]))
