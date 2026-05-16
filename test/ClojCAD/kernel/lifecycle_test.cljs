(ns ClojCAD.kernel.lifecycle-test
  (:require [cljs.test :refer [deftest is testing] :as t]
            [ClojCAD.kernel.lifecycle :as sut]))

(deftest track-adds-object-to-atom
  (let [obj #js {}
        result (sut/track obj)]
    (is (contains? @sut/tracked obj))
    (is (identical? obj result))))

(deftest destroy-removes-object-and-calls-delete
  (let [deleted? (atom false)
        obj #js {:delete (fn [] (reset! deleted? true))}
        _ (sut/track obj)]
    (is (contains? @sut/tracked obj))
    (sut/destroy obj)
    (is (not (contains? @sut/tracked obj)))
    (is @deleted?)))

(deftest destroy-all-clears-all-tracked-objects
  (let [deleted-a? (atom false)
        deleted-b? (atom false)
        a #js {:delete (fn [] (reset! deleted-a? true))}
        b #js {:delete (fn [] (reset! deleted-b? true))}
        _ (sut/track a)
        _ (sut/track b)]
    (sut/destroy-all)
    (is (empty? @sut/tracked))
    (is @deleted-a?)
    (is @deleted-b?)))
