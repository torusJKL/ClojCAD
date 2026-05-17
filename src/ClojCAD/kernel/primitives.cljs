(ns ClojCAD.kernel.primitives
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.lifecycle :as lifecycle]))

(defn- ^js oc []
  @init/oc-instance)

(defn- with-trsf [^js shape f]
  (let [trsf (js/Reflect.construct (.-gp_Trsf_1 (oc)) #js [])
        _ (f trsf)
        loc (js/Reflect.construct (.-TopLoc_Location_4 (oc)) #js [trsf])
        moved (.Moved shape loc false)]
    (lifecycle/track moved)
    moved))

(defn translate
  "Translate (move) a shape by the given x, y, z offsets." [^js shape x y z]
  (with-trsf shape
    (fn [^js trsf]
      (.SetTranslation_1 trsf
        (js/Reflect.construct (.-gp_Vec_4 (oc)) #js [x y z])))))

(defn make-sphere
  "Create a solid sphere with the given radius." [radius]
  (let [ctor (.-BRepPrimAPI_MakeSphere_1 (oc))
        builder (js/Reflect.construct ctor #js [radius])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))

(defn make-box
  "Create a rectangular box with dimensions dx, dy, dz.
   When centered? is true, the box is centered at the origin." ([dx dy dz] (make-box dx dy dz false))
  ([dx dy dz centered?]
   (let [ctor (.-BRepPrimAPI_MakeBox_2 (oc))
         builder (js/Reflect.construct ctor #js [dx dy dz])
         shape (.Shape builder)]
     (.delete builder)
     (lifecycle/track shape)
     (if centered?
       (translate shape (- (/ dx 2)) (- (/ dy 2)) (- (/ dz 2)))
       shape))))

(defn make-cylinder
  "Create a cylinder with the given radius and height.
   When centered? is true, the cylinder is centered along Z." ([radius height] (make-cylinder radius height false))
  ([radius height centered?]
   (let [ctor (.-BRepPrimAPI_MakeCylinder_1 (oc))
         builder (js/Reflect.construct ctor #js [radius height])
         shape (.Shape builder)]
     (.delete builder)
     (lifecycle/track shape)
     (if centered?
       (translate shape 0 0 (- (/ height 2)))
       shape))))

(defn make-cone
  "Create a truncated cone with bottom radius1, top radius2, and the given height." [radius1 radius2 height]
  (let [ctor (.-BRepPrimAPI_MakeCone_1 (oc))
        builder (js/Reflect.construct ctor #js [radius1 radius2 height])
        shape (.Shape builder)]
    (.delete builder)
    (lifecycle/track shape)
    shape))

(defn make-circle
  "Create a circle of the given radius. Returns a face by default, or a wire when wire? is true." ([radius] (make-circle radius false))
  ([radius wire?]
   (when (pos? radius)
     (let [ax2 (js/Reflect.construct (.-gp_Ax2_4 (oc)) #js [
                 (js/Reflect.construct (.-gp_Pnt_3 (oc)) #js [0 0 0])
                 (js/Reflect.construct (.-gp_Dir_3 (oc)) #js [
                   (js/Reflect.construct (.-gp_Vec_4 (oc)) #js [0 0 1])])])
           circ (js/Reflect.construct (.-Geom_Circle_2 (oc)) #js [ax2 radius])
           h-circ (js/Reflect.construct (.-Handle_Geom_Curve_2 (oc)) #js [circ])
           edge-ctor (.-BRepBuilderAPI_MakeEdge_24 (oc))
           edge-builder (js/Reflect.construct edge-ctor #js [h-circ])
           edge (.Edge edge-builder)]
       (.delete edge-builder)
       (let [wire-ctor (.-BRepBuilderAPI_MakeWire_2 (oc))
             wire-builder (js/Reflect.construct wire-ctor #js [edge])
             wire (.Wire wire-builder)]
         (.delete wire-builder)
         (if wire?
           (lifecycle/track wire)
           (let [face-ctor (.-BRepBuilderAPI_MakeFace_15 (oc))
                 face-builder (js/Reflect.construct face-ctor #js [wire false])
                 face (.Face face-builder)]
             (.delete face-builder)
             (lifecycle/track face))))))))

(defn- make-edge-between [p1 p2]
  (let [ctor (.-BRepBuilderAPI_MakeEdge_3 (oc))
        builder (js/Reflect.construct ctor #js [p1 p2])
        edge (.Edge builder)]
    (.delete builder)
    edge))

(defn make-polygon
  "Create a polygon from a sequence of [x y z] points. Returns a face by default,
   or a wire when wire? is true. Requires at least 3 points." ([points] (make-polygon points false))
  ([points wire?]
   (when (>= (count points) 3)
     (let [pts (mapv (fn [[x y z]]
                       (js/Reflect.construct (.-gp_Pnt_3 (oc)) #js [x y (or z 0)]))
                     points)
           edges (mapv make-edge-between pts (concat (rest pts) [(first pts)]))
           wire-builder (js/Reflect.construct (.-BRepBuilderAPI_MakeWire_1 (oc)) #js [])]
       (doseq [e edges]
         (.Add_1 wire-builder e))
       (let [wire (.Wire wire-builder)]
         (.delete wire-builder)
         (if wire?
           (lifecycle/track wire)
           (let [face-ctor (.-BRepBuilderAPI_MakeFace_15 (oc))
                 face-builder (js/Reflect.construct face-ctor #js [wire false])
                 face (.Face face-builder)]
             (.delete face-builder)
             (lifecycle/track face))))))))


(defn extrude
  "Extrude a planar face along a direction vector [dx dy dz] to create a solid.
   The face must be a valid planar face (e.g. from make-polygon or make-circle)." ([face direction]
   (let [[dx dy dz] direction]
     (when face
       (when (or (not (zero? dx)) (not (zero? dy)) (not (zero? dz)))
         (let [ctor (.-BRepPrimAPI_MakePrism_1 (oc))
               vec (js/Reflect.construct (.-gp_Vec_4 (oc)) #js [dx dy dz])
               builder (js/Reflect.construct ctor #js [face vec false true])
               shape (.Shape builder)]
           (.delete builder)
           (lifecycle/track shape)
           shape))))))

(defn rotate
  "Rotate a shape around the given axis vector by the specified degrees." [^js shape axis-x axis-y axis-z degrees]
  (with-trsf shape
    (fn [^js trsf]
      (.SetRotation_1 trsf
        (js/Reflect.construct (.-gp_Ax1_2 (oc)) #js [
          (js/Reflect.construct (.-gp_Pnt_3 (oc)) #js [0 0 0])
          (js/Reflect.construct (.-gp_Dir_3 (oc)) #js [
            (js/Reflect.construct (.-gp_Vec_4 (oc)) #js [axis-x axis-y axis-z])
          ])
        ])
        (* degrees 0.0174533)))))
