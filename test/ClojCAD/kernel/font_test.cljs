(ns ClojCAD.kernel.font-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.kernel.font :as sut]))

(deftest empty-registry-returns-nil
  (is (nil? (sut/list-fonts))))

(deftest font-info-returns-nil-for-unknown
  (is (nil? (sut/font-info "NonExistent"))))
