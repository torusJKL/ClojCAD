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
                  :let [info (reg/lookup name)]]
            (try
              (let [result ((:fn info) new-val)
                    mesh (kernel/tessellate (:shape result))
                    tags (reduce-kv (fn [m k v] (assoc m k (kernel/tessellate v))) {} (:tags result))
                    new-tag-names (set (keys tags))
                    existing-tags-visible (:tags-visible entry {})
                    tags-visible (merge (zipmap new-tag-names (repeat true))
                                        (select-keys existing-tags-visible new-tag-names))]
                (swap! scene assoc name
                  (dissoc (assoc entry :mesh mesh :tags tags :tags-visible tags-visible) :error)))
              (catch :default e
                (swap! scene assoc-in [name :error] (str e)))))
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
         merged (merge @params params-override)]
     (try
       (let [result (model merged)
             mesh (kernel/tessellate (:shape result))
             tags (reduce-kv (fn [m k v] (assoc m k (kernel/tessellate v))) {} (:tags result))
             tags-visible (zipmap (keys tags) (repeat true))]
         (swap! scene assoc name
           {:mesh mesh
            :tags tags
            :tags-visible tags-visible
            :opts (merge opts display-opts)
            :visible? true}))
       (catch :default e
         (swap! scene assoc name
           {:mesh nil
            :tags {}
            :tags-visible {}
            :opts (merge opts display-opts)
            :visible? true
            :error (str e)})))
     (when-let [cb @on-update] (cb)))))

(defn hide [model-name]
  (swap! scene assoc-in [model-name :visible?] false)
  (when-let [cb @on-update] (cb)))

(defn show-model [model-name]
  (swap! scene assoc-in [model-name :visible?] true)
  (when-let [cb @on-update] (cb)))

(defn hide-tag [model-name label]
  (swap! scene assoc-in [model-name :tags-visible label] false)
  (when-let [cb @on-update] (cb)))

(defn show-tag [model-name label]
  (swap! scene assoc-in [model-name :tags-visible label] true)
  (when-let [cb @on-update] (cb)))

(defn remove-model [model-name]
  (swap! scene dissoc model-name)
  (when-let [cb @on-update] (cb)))

(defn set-opacity [model-name opacity]
  (swap! scene assoc-in [model-name :opts :opacity] opacity)
  (when-let [cb @on-update] (cb)))
