(ns ClojCAD.viewport.shape-adapter-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.viewport.shape-adapter :as sut]))

(deftest tessellation-to-shape-sets-vertices
  (let [verts (js/Float32Array. [0 0 0 1 0 0 0 1 0])
        indices (js/Uint32Array. [0 1 2])
        normals (js/Float32Array. [0 0 1 0 0 1 0 0 1])
        mesh {:vertices verts :indices indices :normals normals}
        shape (sut/tessellation->shape mesh)]
    (is (= verts (.-vertices shape)))
    (is (= indices (.-triangles shape)))
    (is (= normals (.-normals shape)))))

(deftest tessellation-to-shape-with-edges
  (let [verts (js/Float32Array. 3)
        indices (js/Uint32Array. 3)
        normals (js/Float32Array. 3)
        edges (js/Float32Array. 6)
        mesh {:vertices verts :indices indices :normals normals :edges edges}
        shape (sut/tessellation->shape mesh)]
    (is (= edges (.-edges shape)))))

(deftest tessellation-to-shape-without-edges
  (let [verts (js/Float32Array. 3)
        indices (js/Uint32Array. 3)
        normals (js/Float32Array. 3)
        mesh {:vertices verts :indices indices :normals normals}
        shape (sut/tessellation->shape mesh)]
    (is (nil? (.-edges shape)))))
