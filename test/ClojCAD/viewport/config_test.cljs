(ns ClojCAD.viewport.config-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.viewport.config :as sut]))

(deftest default-shape-color-is-set
  (is (number? (sut/get-default-shape-color)))
  (is (pos? (sut/get-default-shape-color))))

(deftest set-default-shape-color-updates
  (let [original (sut/get-default-shape-color)]
    (sut/set-default-shape-color! 0xff0000)
    (is (= 0xff0000 (sut/get-default-shape-color)))
    ;; restore
    (sut/set-default-shape-color! original)))
