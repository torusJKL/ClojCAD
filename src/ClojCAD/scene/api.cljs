(ns ClojCAD.scene.api
  (:require [clojure.string :as str]
            [ClojCAD.scene.manager :as sm]))

(defn- glob->regex [pattern]
  (if (instance? js/RegExp pattern)
    pattern
    (js/RegExp. (str "^" (str/replace pattern "*" ".*") "$"))))

(defn list-objects
  ([] @sm/scene)
  ([filters]
   (let [{:keys [tag visibility name-matching]} filters
         tag-str (when tag (name tag))
         re (when name-matching (glob->regex name-matching))]
     (reduce-kv (fn [acc name-str entry]
                  (if (and (or (nil? tag) (contains? (:tags entry) tag))
                           (or (nil? visibility)
                               (if (= :visible visibility)
                                 (:visible? entry)
                                 (not (:visible? entry))))
                           (or (nil? name-matching)
                               (.test re name-str)))
                    (assoc acc name-str entry)
                    acc))
                {} @sm/scene))))

(defn list-tags []
  (into #{}
    (comp (map (fn [[_ entry]] (keys (:tags entry))))
          cat
          (map keyword))
    @sm/scene))

(def show-model sm/show-model)
(def hide-model sm/hide-model)
(def toggle-model sm/toggle-model)
(def show-all sm/show-all)
(def hide-all sm/hide-all)
(def add-tags sm/add-tags)
(def remove-tags sm/remove-tags)
