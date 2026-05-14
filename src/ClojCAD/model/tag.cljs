(ns ClojCAD.model.tag)

(def ^:dynamic *scene-context* nil)

(defn tag [label shape]
  (when (some? *scene-context*)
    (swap! *scene-context* assoc label shape))
  shape)
