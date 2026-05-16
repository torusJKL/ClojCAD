(ns ClojCAD.kernel.booleans-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.primitives :as prim]
            [ClojCAD.kernel.booleans :as sut]))

;; WASM-dependent tests — skipped automatically when WASM init fails in Node.js
;; Run via browser REPL for full OCCT integration testing

(deftest fuse-overlapping-spheres
  (let [a (prim/make-sphere 10)
        b (prim/translate (prim/make-sphere 10) 16 0 0)
        result (sut/fuse a b)]
    (is (some? result))
    (is (false? (.IsNull result)))))

(deftest cut-box-with-cylinder
  (let [box (prim/make-box 20 20 20)
        cyl (prim/make-cylinder 5 30)
        result (sut/cut box cyl)]
    (is (some? result))
    (is (false? (.IsNull result)))))

(deftest common-overlapping-shapes
  (let [a (prim/make-box 10 10 10)
        b (prim/translate (prim/make-box 10 10 10) 5 5 5)
        result (sut/common a b)]
    (is (some? result))
    (is (false? (.IsNull result)))))

(deftest non-overlapping-common-returns-nil
  (let [a (prim/make-box 10 10 10)
        b (prim/translate (prim/make-box 10 10 10) 100 100 100)
        result (sut/common a b)]
    (is (nil? result))))

(deftest variadic-fuse-chaining
  (let [a (prim/make-sphere 5)
        b (prim/translate (prim/make-sphere 5) 8 0 0)
        c (prim/translate (prim/make-sphere 5) 16 0 0)
        result (sut/fuse a b c)]
    (is (some? result))
    (is (false? (.IsNull result)))))
