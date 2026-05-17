(ns ClojCAD.model.tag)

(def ^:dynamic *scene-context* nil)

(defn tag
  "Tag a shape with a label within the current model context. Tags allow individual
   sub-shapes of a model to be referenced separately (e.g. for visibility control).
   The *scene-context* dynamic binding is used to collect tags during model evaluation.
   Returns the shape unchanged." [label shape]
  (when (some? *scene-context*)
    (swap! *scene-context* assoc label shape))
  shape)
