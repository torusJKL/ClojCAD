(ns ClojCAD.kernel.text3d-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.text3d :as sut]))

(deftest text3d-with-unknown-font-returns-nil
  (is (nil? (sut/text3d "Hello" 36 :font "NonExistent"))))

(deftest text3d-with-default-args-returns-shape
  (is (some? (sut/text3d "Hello" 36))))

(deftest text3d-with-custom-font-returns-shape
  (is (some? (sut/text3d "Hello" 36 :font "Cousine-Bold"))))

(deftest text3d-with-zero-height-returns-shape
  (is (some? (sut/text3d "Hello" 36 :height 0))))
