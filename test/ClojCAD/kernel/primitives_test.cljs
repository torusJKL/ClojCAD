(ns ClojCAD.kernel.primitives-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.primitives :as sut]))

(deftest make-sphere-returns-shape
  (let [shape (sut/make-sphere 10)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-box-returns-shape
  (let [shape (sut/make-box 10 20 30)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-cylinder-returns-shape
  (let [shape (sut/make-cylinder 5 20)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-cone-returns-shape
  (let [shape (sut/make-cone 5 10 15)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest translate-moves-shape
  (let [box (sut/make-box 10 10 10)
        moved (sut/translate box 5 10 15)]
    (is (some? moved))
    (is (false? (.IsNull moved)))))

(deftest rotate-rotates-shape
  (let [box (sut/make-box 10 10 10)
        rotated (sut/rotate box 0 0 1 45)]
    (is (some? rotated))
    (is (false? (.IsNull rotated)))))
