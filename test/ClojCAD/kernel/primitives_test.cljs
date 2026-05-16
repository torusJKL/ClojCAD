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

(deftest make-box-centered-returns-shape
  (let [shape (sut/make-box 10 20 30 true)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-cylinder-returns-shape
  (let [shape (sut/make-cylinder 5 20)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-cylinder-centered-returns-shape
  (let [shape (sut/make-cylinder 5 20 true)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-cone-returns-shape
  (let [shape (sut/make-cone 5 10 15)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-circle-face-returns-shape
  (let [shape (sut/make-circle 5)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-circle-wire-returns-shape
  (let [shape (sut/make-circle 5 true)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-circle-invalid-radius-returns-nil
  (is (nil? (sut/make-circle 0)))
  (is (nil? (sut/make-circle -1))))

(deftest make-polygon-face-returns-shape
  (let [shape (sut/make-polygon [[0 0] [10 0] [10 10] [0 10]])]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-polygon-wire-returns-shape
  (let [shape (sut/make-polygon [[0 0] [10 0] [10 10] [0 10]] true)]
    (is (some? shape))
    (is (false? (.IsNull shape)))))

(deftest make-polygon-insufficient-points-returns-nil
  (is (nil? (sut/make-polygon [])))
  (is (nil? (sut/make-polygon [[1 2] [3 4]]))))

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

(deftest extrude-face-returns-shape
  (let [face (sut/make-circle 5)
        extruded (sut/extrude face [0 0 10])]
    (is (some? extruded))
    (is (false? (.IsNull extruded)))))

(deftest extrude-nil-face-returns-nil
  (is (nil? (sut/extrude nil [0 0 10]))))
