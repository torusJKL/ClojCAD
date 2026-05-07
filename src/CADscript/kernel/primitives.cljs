(ns CADscript.kernel.primitives
  (:require ["three" :as three]))

(defn- sphere-mesh-data [radius widthSeg heightSeg]
  (let [geo (three/SphereGeometry. radius widthSeg heightSeg)
        pos (.. geo -attributes -position -array)
        idx (.getIndex geo)
        vertices (vec (js/Array.from pos))
        indices (vec (js/Array.from (.. idx -array)))]
    (.dispose geo)
    {:vertices vertices :indices indices}))

(defn- box-mesh-data [w d h]
  (let [geo (three/BoxGeometry. w d h)
        pos (.. geo -attributes -position -array)
        idx (.getIndex geo)
        vertices (vec (js/Array.from pos))
        indices (vec (js/Array.from (.. idx -array)))]
    (.dispose geo)
    {:vertices vertices :indices indices}))

(defn make-sphere [radius]
  (sphere-mesh-data radius 24 16))

(defn make-box [dx dy dz]
  (box-mesh-data dx dy dz))
