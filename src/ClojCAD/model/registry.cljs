(ns ClojCAD.model.registry)

(defonce models (atom {}))

(defn register! [model-name entry]
  (swap! models assoc model-name entry))

(defn lookup [model-name]
  (get @models model-name))

(defn registered-keys []
  (keys @models))
