(ns ClojCAD.model.core
  (:require [ClojCAD.model.tag :as tag]
            [ClojCAD.model.registry :as registry]))

(defn reactive-model
  "Create a reactive parametric model. The model function f is called with a params map
   and its result is cached keyed by params. The model is registered in the global registry.
   Returns the model function with metadata containing :name, :param-keys, and :opts." [name param-keys f opts]
  (let [cache-key (atom ::empty)
        cache-val (atom nil)
        model-fn
        (fn [params]
          (if (= params @cache-key)
            @cache-val
            (let [tags (atom {})]
              (binding [tag/*scene-context* tags]
                (let [result (f params)
                      shape (if (map? result) (:shape result) result)
                              tags-map (when (map? result) (:tags result))
                              extra (when (map? result) (dissoc result :shape :tags))
                              merged-tags (merge @tags tags-map)]
                  (reset! cache-key params)
                  (reset! cache-val (merge {:shape shape :tags merged-tags} extra))
                  @cache-val)))))]
    (registry/register! name {:fn model-fn :param-keys param-keys :opts opts})
    (with-meta model-fn
      {:name name
       :param-keys param-keys
       :opts opts})))
