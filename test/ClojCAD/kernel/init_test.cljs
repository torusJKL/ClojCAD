(ns ClojCAD.kernel.init-test
  (:require [cljs.test :refer [deftest is]]
            [ClojCAD.kernel.init :as sut]))

(deftest wasm-loads-successfully
  (is (some? @sut/oc-instance))
  (is (false? @sut/loading?)))
