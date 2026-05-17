(ns ClojCAD.scene.api
  (:require [clojure.string :as str]
            [ClojCAD.scene.manager :as sm]))

(defn- glob->regex [pattern]
  (if (instance? js/RegExp pattern)
    pattern
    (js/RegExp. (str "^" (str/replace pattern "*" ".*") "$"))))

(defn list-objects
  "List objects in the scene with optional filters. Filters can include :tag, :visibility,
   and :name-matching (supports glob * wildcards). Returns a map of name -> entry." ([] @sm/scene)
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

(defn list-tags
  "Return a set of all tag labels currently used across all objects in the scene." []
  (into #{}
    (comp (map (fn [[_ entry]] (keys (:tags entry))))
          cat
          (map keyword))
    @sm/scene))

(def show-model
  "Show a model or specific tag in the viewport."
  sm/show-model)
(def hide-model
  "Hide a model or specific tag in the viewport."
  sm/hide-model)
(def toggle-model
  "Toggle visibility of a model or tag."
  sm/toggle-model)
(def show-all
  "Show all models in the scene."
  sm/show-all)
(def hide-all
  "Hide all models in the scene."
  sm/hide-all)
(def add-tags
  "Add tagged sub-shapes to an existing model."
  sm/add-tags)
(def remove-tags
  "Remove tagged sub-shapes from a model."
  sm/remove-tags)
