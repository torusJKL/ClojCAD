(ns ClojCAD.scene.manager
  (:require [ClojCAD.model.registry :as reg]
            [ClojCAD.kernel.api :as kernel]
            [ClojCAD.viewport.viewer :as vw]
            [ClojCAD.viewport.shape-adapter :as sa]))

(defonce params (atom {}))
(defonce scene (atom {}))

(defn- model-path [name]
  (str "/root/" name))

(defn- tag-path [name tag-label]
  (str "/root/" name "/" tag-label))

(defn- notify-callback [changes]
  (when-let [states-change (.-states changes)]
    (when-let [states (.-new states-change)]
      (doseq [[path state-arr] (js->clj states)]
        (let [parts (.split path "/")
              parts (remove empty? parts)]
          (cond
            (= (count parts) 2)
            (let [model-name (second parts)
                  visible? (pos? (first state-arr))]
              (swap! scene assoc-in [model-name :visible?] visible?))
            (= (count parts) 3)
            (let [model-name (second parts)
                  tag-label (nth parts 2)
                  visible? (pos? (first state-arr))]
              (swap! scene assoc-in [model-name :tags-visible tag-label] visible?))))))))

(vw/set-notify-handler! notify-callback)

(defonce watch-installed
  (do
    (add-watch params :scene-manager
      (fn [_ _ old-val new-val]
        (let [all-keys (set (concat (keys old-val) (keys new-val)))
              delta (set (filter #(not= (get old-val %) (get new-val %)) all-keys))
              dirty (filter (fn [[name _]]
                              (when-let [info (reg/lookup (symbol name))]
                                (some (set (:param-keys info)) delta)))
                            @scene)
              viewer @vw/*viewer]
          (doseq [[name entry] dirty
                  :let [info (reg/lookup (symbol name))]]
            (try
              (let [result ((:fn info) new-val)
                    mesh (kernel/tessellate (:shape result))
                    tags (reduce-kv (fn [m k v] (assoc m k (kernel/tessellate v))) {} (:tags result))
                    new-tag-names (set (keys tags))
                    existing-tags-visible (:tags-visible entry {})
                    tags-visible (merge (zipmap new-tag-names (repeat true))
                                        (select-keys existing-tags-visible new-tag-names))
                    part (sa/build-part name mesh (:opts entry))]
                (swap! scene assoc name
                  (dissoc (assoc entry :mesh mesh :tags tags :tags-visible tags-visible) :error))
                (when viewer
                  (.updatePart viewer (model-path name) part #js {:skipBounds true})
                  (doseq [[tag-label tag-mesh] tags]
                    (let [child (sa/build-child-part name tag-label tag-mesh)]
                      (.updatePart viewer (tag-path name tag-label) child #js {:skipBounds true})))))
              (catch :default e
                (swap! scene assoc-in [name :error] (str e)))))
          (when (and viewer (seq dirty))
            (.updateBounds viewer)))))
    true))

(defn show
  ([model] (show model {} {}))
  ([model params-override] (show model params-override {}))
  ([model params-override display-opts]
   (let [{:keys [name opts]} (meta model)
         name-str (str name)
         merged (merge @params params-override)
         viewer @vw/*viewer]
     (try
       (let [result (model merged)
             mesh (kernel/tessellate (:shape result))
             tag-pos (or (:tag-pos result) {})
             tags (reduce-kv (fn [m k v] (assoc m k (kernel/tessellate v))) {} (:tags result))
             tags-visible (zipmap (keys tags) (repeat true))
             part (sa/build-part name-str mesh (merge opts display-opts))]
         (swap! scene assoc name-str
           {:mesh mesh
            :tags tags
            :tag-pos tag-pos
            :tags-visible tags-visible
            :opts (merge opts display-opts)
            :visible? true})
          (if @vw/*rendered?
            (when viewer
              (if (seq tags)
                (let [tag-parts (vec (for [[tag-label tag-mesh] tags]
                                      (sa/build-child-part name-str tag-label tag-mesh (get tag-pos tag-label))))
                      tree (sa/build-shapes-tree name-str part tag-parts)]
                  (.addPart viewer "/root" tree))
                (.addPart viewer "/root" part))
              (.presetCamera viewer "iso"))
            (let [model-part (if (seq tags)
                              (let [tag-parts (vec (for [[tag-label tag-mesh] tags]
                                                    (sa/build-child-part name-str tag-label tag-mesh (get tag-pos tag-label))))
                                    tree (sa/build-shapes-tree name-str part tag-parts)]
                                tree)
                              part)
                  shapes #js {:version 3 :id "/root" :name "root"
                              :parts #js [model-part]}]
              (when (not (seq tags))
                (aset model-part "id" (str "/root/" name-str)))
              (vw/render-initial! shapes))))
        (catch :default e
         (swap! scene assoc name-str
           {:mesh nil
            :tags {}
            :tags-visible {}
            :opts (merge opts display-opts)
            :visible? true
            :error (str e)}))))))

(defn hide-model [model-name]
  (let [name-str (name model-name)
        path (model-path name-str)]
    (swap! scene assoc-in [name-str :visible?] false)
    (when-let [viewer @vw/*viewer]
      (.setObject viewer path 0 0 false false)
      (.setState viewer path #js [0 1]))))

(defn show-model [model-name]
  (let [name-str (name model-name)
        path (model-path name-str)]
    (swap! scene assoc-in [name-str :visible?] true)
    (when-let [viewer @vw/*viewer]
      (.setObject viewer path 1 0 false false)
      (.setState viewer path #js [1 1]))))

(defn hide-tag [model-name label]
  (let [name-str (name model-name)
        tag-str (name label)
        path (tag-path name-str tag-str)]
    (swap! scene assoc-in [name-str :tags-visible tag-str] false)
    (when-let [viewer @vw/*viewer]
      (.setObject viewer path 0 0 false false)
      (.setState viewer path #js [0 1]))))

(defn show-tag [model-name label]
  (let [name-str (name model-name)
        tag-str (name label)
        path (tag-path name-str tag-str)]
    (swap! scene assoc-in [name-str :tags-visible tag-str] true)
    (when-let [viewer @vw/*viewer]
      (.setObject viewer path 1 0 false false)
      (.setState viewer path #js [1 1]))))

(defn remove-model [model-name]
  (let [name-str (name model-name)]
    (swap! scene dissoc name-str)
    (when-let [viewer @vw/*viewer]
      (.removePart viewer (model-path name-str)))))

(defn set-opacity [model-name opacity]
  (let [name-str (name model-name)]
    (swap! scene assoc-in [name-str :opts :opacity] opacity)
    (let [{:keys [mesh opts]} (get @scene name-str)
          viewer @vw/*viewer]
      (when (and viewer mesh)
        (.updatePart viewer (model-path name-str) (sa/build-part name-str mesh (assoc opts :opacity opacity)))))))
