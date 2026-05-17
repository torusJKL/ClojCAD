(ns ClojCAD.kernel.lifecycle)

(defonce tracked (atom #{}))

(defn track
  "Register a TopoDS_Shape (or other OpenCASCADE object) for automatic lifecycle
   management. Returns the object itself." [obj]
  (swap! tracked conj obj)
  obj)

(defn destroy
  "Explicitly delete an OpenCASCADE object and remove it from the tracking set." [obj]
  (.delete obj)
  (swap! tracked disj obj))

(defn destroy-all
  "Delete all tracked OpenCASCADE objects and reset the tracking set." []
  (doseq [obj @tracked]
    (try (.delete obj) (catch :default _)))
  (reset! tracked #{}))
