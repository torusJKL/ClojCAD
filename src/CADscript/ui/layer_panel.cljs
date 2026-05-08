(ns CADscript.ui.layer-panel
  (:require [CADscript.scene.manager :as sm]
            [CADscript.kernel.api :as kernel]))

(defn- build-items [scene]
  (reduce-kv
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
    [] scene))

(defn layer-panel []
  (let [loading @kernel/loading?
        err @kernel/error
        scene @sm/scene]
    [:div.layer-panel
     [:h3 "Layers"]
     (cond
       loading
       [:div.loading "Loading OpenCASCADE WASM..."]
       err
       [:div.error (str "Init error: " err)]
       :else
       (into [:div] (build-items scene)))]))
