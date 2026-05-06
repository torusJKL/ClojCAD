(ns CADscript.model.core
  (:require [CADscript.model.tag :as tag]
            [CADscript.model.registry :as registry]))

(defn reactive-model [name param-keys f opts]
  (let [cache-key (atom ::empty)
        cache-val (atom nil)
        model-fn
        (fn [params]
          (if (= params @cache-key)
            @cache-val
            (let [tags (atom {})]
              (binding [tag/*scene-context* tags]
                (let [shape (f params)]
                  (reset! cache-key params)
                  (reset! cache-val {:shape shape :tags @tags})
                  @cache-val)))))]
    (registry/register! name {:fn model-fn :param-keys param-keys :opts opts})
    (with-meta model-fn
      {:name name
       :param-keys param-keys
       :opts opts})))
