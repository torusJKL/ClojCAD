(ns ClojCAD.kernel.mesh-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.primitives :as prim]
            [ClojCAD.kernel.mesh :as sut]))

;; WASM-dependent tests — skipped automatically when WASM init fails in Node.js
;; Run via browser REPL for full OCCT integration testing

(deftest tessellate-returns-expected-keys
  (let [shape (prim/make-sphere 10)
        mesh (sut/tessellate shape)]
    (is (map? mesh))
    (is (contains? mesh :vertices))
    (is (contains? mesh :normals))
    (is (contains? mesh :indices))
    (is (contains? mesh :edges))
    (is (contains? mesh :obj-vertices))
    (is (contains? mesh :face-types))
    (is (contains? mesh :edge-types))
    (is (contains? mesh :triangles-per-face))
    (is (contains? mesh :segments-per-edge))))

(deftest tessellate-returns-typed-arrays
  (let [shape (prim/make-sphere 10)
        mesh (sut/tessellate shape)]
    (is (instance? js/Float32Array (:vertices mesh)))
    (is (instance? js/Float32Array (:normals mesh)))
    (is (instance? js/Uint32Array (:indices mesh)))))

(deftest tessellate-accepts-custom-deviation
  (let [shape (prim/make-sphere 10)
        mesh-default (sut/tessellate shape)
        mesh-fine (sut/tessellate shape 0.01)]
    (is (map? mesh-fine))
    ;; finer deviation produces more vertices
    (is (>= (.-length (:vertices mesh-fine))
            (.-length (:vertices mesh-default))))))
