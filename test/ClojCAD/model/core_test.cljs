(ns ClojCAD.model.core-test
  (:require [cljs.test :refer [deftest is testing]]
            [ClojCAD.model.core :as sut]
            [ClojCAD.model.registry :as reg]))

(deftest reactive-model-caches-same-params
  (let [call-count (atom 0)
        model-fn (sut/reactive-model ::cache-test [:x]
                   (fn [params]
                     (swap! call-count inc)
                     {:shape (str "result-" @call-count)})
                   {})]
    (is (= {:shape "result-1" :tags {}} (model-fn {:x 1})))
    (is (= {:shape "result-1" :tags {}} (model-fn {:x 1})))
    (is (= 1 @call-count))))

(deftest reactive-model-recomputes-on-different-params
  (let [call-count (atom 0)
        model-fn (sut/reactive-model ::recompute-test [:x]
                   (fn [params]
                     (swap! call-count inc)
                     {:shape (str "result-" @call-count)})
                   {})]
    (is (= {:shape "result-1" :tags {}} (model-fn {:x 1})))
    (is (= {:shape "result-2" :tags {}} (model-fn {:x 2})))
    (is (= 2 @call-count))))
