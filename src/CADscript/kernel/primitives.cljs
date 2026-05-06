(ns CADscript.kernel.primitives
  (:require [CADscript.kernel.init :as init]))

(defn make-sphere [radius]
  (let [oc @init/oc-instance
        maker (oc.BRepPrimAPI_MakeSphere. radius)]
    (.Shape maker)))

(defn make-box [dx dy dz]
  (let [oc @init/oc-instance
        maker (oc.BRepPrimAPI_MakeBox. dx dy dz)]
    (.Shape maker)))
