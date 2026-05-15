(ns ClojCAD.kernel.primitives
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.lifecycle :as lifecycle]))

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

(defn- with-trsf [shape f]
  (let [trsf (js/Reflect.construct (.-gp_Trsf_1 (oc)) #js [])
        _ (f trsf)
        loc (js/Reflect.construct (.-TopLoc_Location_4 (oc)) #js [trsf])
        moved (.Moved shape loc false)]
    (lifecycle/track moved)
    moved))

(defn translate [shape x y z]
  (with-trsf shape
    (fn [trsf]
      (.SetTranslation_1 trsf
        (js/Reflect.construct (.-gp_Vec_4 (oc)) #js [x y z])))))

(defn rotate [shape axis-x axis-y axis-z degrees]
  (with-trsf shape
    (fn [trsf]
      (.SetRotation_1 trsf
        (js/Reflect.construct (.-gp_Ax1_2 (oc)) #js [
          (js/Reflect.construct (.-gp_Pnt_3 (oc)) #js [0 0 0])
          (js/Reflect.construct (.-gp_Dir_3 (oc)) #js [
            (js/Reflect.construct (.-gp_Vec_4 (oc)) #js [axis-x axis-y axis-z])
          ])
        ])
        (* degrees 0.0174533)))))
