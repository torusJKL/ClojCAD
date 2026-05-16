(ns ClojCAD.model.tag-test
  (:require [cljs.test :refer [deftest is testing]]
            [ClojCAD.model.tag :as sut]))

(deftest tag-records-shape-in-context
  (let [ctx (atom {})
        shape #js {:type :dummy}]
    (binding [sut/*scene-context* ctx]
      (let [result (sut/tag :my-label shape)]
        (is (identical? shape result))
        (is (= shape (get @ctx :my-label)))))))

(deftest tag-is-noop-outside-context
  (let [shape #js {:type :dummy}]
    (is (identical? shape (sut/tag :my-label shape)))))
