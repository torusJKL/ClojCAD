(ns CADscript.scene.manager
  (:require [CADscript.model.registry :as reg]
            [CADscript.kernel.api :as kernel]))

(defonce params (atom {}))
(defonce scene (atom {}))
(defonce on-update (atom nil))

(defonce watch-installed
  (do
    (add-watch params :scene-manager
      (fn [_ _ old-val new-val]
        (let [all-keys (set (concat (keys old-val) (keys new-val)))
              delta (set (filter #(not= (get old-val %) (get new-val %)) all-keys))
              dirty (filter (fn [[name _]]
                              (when-let [info (reg/lookup name)]
                                (some (set (:param-keys info)) delta)))
                            @scene)]
          (doseq [[name entry] dirty
                  :let [info (reg/lookup name)
                        result ((:fn info) new-val)
                        mesh (kernel/tessellate (:shape result))
                        tags (reduce-kv (fn [m k v] (assoc m k (kernel/tessellate v))) {} (:tags result))]]
            (swap! scene assoc name
              (assoc entry :mesh mesh :tags tags)))
          (when (seq dirty)
            (when-let [cb @on-update] (cb))))))
    true))

(defn set-on-update! [f]
  (reset! on-update f))

(defn show
  ([model] (show model {} {}))
  ([model params-override] (show model params-override {}))
  ([model params-override display-opts]
   (let [{:keys [name opts]} (meta model)
         merged (merge @params params-override)
         result (model merged)
         mesh (kernel/tessellate (:shape result))
         tags (reduce-kv (fn [m k v] (assoc m k (kernel/tessellate v))) {} (:tags result))]
     (swap! scene assoc name
       {:mesh mesh
        :tags tags
        :opts (merge opts display-opts)
        :visible? true})
     (when-let [cb @on-update] (cb)))))

(defn hide [model-name]
  (swap! scene assoc-in [model-name :visible?] false)
  (when-let [cb @on-update] (cb)))

(defn show-model [model-name]
  (swap! scene assoc-in [model-name :visible?] true)
  (when-let [cb @on-update] (cb)))

(defn set-opacity [model-name opacity]
  (swap! scene assoc-in [model-name :opts :opacity] opacity)
  (when-let [cb @on-update] (cb)))
