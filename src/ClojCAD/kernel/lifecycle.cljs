(ns ClojCAD.kernel.lifecycle)

(defonce tracked (atom #{}))

(defn track [obj]
  (swap! tracked conj obj)
  obj)

(defn destroy [obj]
  (.delete obj)
  (swap! tracked disj obj))

(defn destroy-all []
  (doseq [obj @tracked]
    (try (.delete obj) (catch :default _)))
  (reset! tracked #{}))
