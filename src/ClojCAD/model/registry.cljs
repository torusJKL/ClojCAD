(ns ClojCAD.model.registry)

(defonce models (atom {}))

(defn register!
  "Register a model entry under the given model-name in the global registry." [model-name entry]
  (swap! models assoc model-name entry))

(defn lookup
  "Look up a registered model entry by name. Returns {:fn ... :param-keys ... :opts ...} or nil." [model-name]
  (get @models model-name))

(defn registered-keys
  "Return a list of all registered model names." []
  (keys @models))
