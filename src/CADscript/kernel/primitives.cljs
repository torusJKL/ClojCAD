(ns CADscript.kernel.primitives
  (:require [CADscript.kernel.init :as init]
            [CADscript.kernel.lifecycle :as lifecycle]))

(defn- oc []
  @init/oc-instance)

(defn make-sphere [radius]
  (let [ctor (.-BRepPrimAPI_MakeSphere_1 (oc))
        builder (js/Reflect.construct ctor #js [radius])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))

(defn make-box [dx dy dz]
  (let [ctor (.-BRepPrimAPI_MakeBox_2 (oc))
        builder (js/Reflect.construct ctor #js [dx dy dz])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))

(defn make-cylinder [radius height]
  (let [ctor (.-BRepPrimAPI_MakeCylinder_1 (oc))
        builder (js/Reflect.construct ctor #js [radius height])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))

(defn make-cone [radius1 radius2 height]
  (let [ctor (.-BRepPrimAPI_MakeCone_1 (oc))
        builder (js/Reflect.construct ctor #js [radius1 radius2 height])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))
