(ns ClojCAD.model.registry-test
  (:require [cljs.test :refer [deftest is testing]]
            [ClojCAD.model.registry :as sut]))

(deftest register-adds-entry
  (let [entry {:fn (fn []) :param-keys [:r] :opts {}}]
    (sut/register! ::my-model entry)
    (is (= entry (sut/lookup ::my-model)))))

(deftest lookup-returns-nil-for-unknown
  (is (nil? (sut/lookup ::nonexistent))))

(deftest registered-keys-includes-registered-names
  (sut/register! ::test-keys {:fn (fn []) :param-keys [] :opts {}})
  (is (some #{::test-keys} (sut/registered-keys))))
