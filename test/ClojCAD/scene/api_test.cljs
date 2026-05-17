(ns ClojCAD.scene.api-test
  (:require [cljs.test :refer [deftest is testing use-fixtures]]
            [ClojCAD.scene.api :as sut]
            [ClojCAD.scene.manager :as sm]))

(use-fixtures :each
  (fn [f]
    (reset! sm/params {})
    (reset! sm/scene {})
    (reset! sm/current-model nil)
    (f)))

(def sample-entry
  {:occt-shape nil
   :mesh nil
   :tags {:sphere nil :box nil}
   :tags-visible {"sphere" true "box" false}
   :opts {}
   :visible? true})

(def hidden-entry
  (assoc sample-entry :visible? false))

(def tagless-entry
  (assoc sample-entry :tags {} :tags-visible {}))

(deftest list-objects-returns-empty-map-when-scene-empty
  (is (= {} (sut/list-objects))))

(deftest list-objects-returns-all-entries
  (swap! sm/scene assoc "model-a" sample-entry "model-b" tagless-entry)
  (let [result (sut/list-objects)]
    (is (= 2 (count result)))
    (is (contains? result "model-a"))
    (is (contains? result "model-b"))))

(deftest list-objects-filters-by-tag
  (swap! sm/scene assoc "model-a" sample-entry "model-b" tagless-entry)
  (let [result (sut/list-objects {:tag :sphere})]
    (is (= 1 (count result)))
    (is (contains? result "model-a"))))

(deftest list-objects-filters-by-nonexistent-tag-returns-empty
  (swap! sm/scene assoc "model-a" sample-entry)
  (is (= {} (sut/list-objects {:tag :nonexistent}))))

(deftest list-objects-filters-by-visible
  (swap! sm/scene assoc "model-a" sample-entry "model-b" hidden-entry)
  (let [result (sut/list-objects {:visibility :visible})]
    (is (= 1 (count result)))
    (is (contains? result "model-a"))))

(deftest list-objects-filters-by-hidden
  (swap! sm/scene assoc "model-a" sample-entry "model-b" hidden-entry)
  (let [result (sut/list-objects {:visibility :hidden})]
    (is (= 1 (count result)))
    (is (contains? result "model-b"))))

(deftest list-objects-filters-by-name-matching
  (swap! sm/scene assoc "foo-model" sample-entry "bar-model" tagless-entry)
  (let [result (sut/list-objects {:name-matching "foo*"})]
    (is (= 1 (count result)))
    (is (contains? result "foo-model"))))

(deftest list-objects-combines-filters
  (swap! sm/scene assoc
    "foo-a" (assoc sample-entry :visible? true)
    "foo-b" (assoc sample-entry :visible? false)
    "bar" tagless-entry)
  (let [result (sut/list-objects {:name-matching "foo*" :visibility :visible :tag :box})]
    (is (= 1 (count result)))
    (is (contains? result "foo-a"))))

(deftest list-objects-ignores-unknown-filter-keys
  (swap! sm/scene assoc "model-a" sample-entry)
  (is (= {"model-a" sample-entry} (sut/list-objects {:unknown :val}))))

(deftest list-tags-returns-unique-keywords
  (swap! sm/scene assoc
    "model-a" {:tags {:sphere nil :box nil}}
    "model-b" {:tags {:sphere nil :cone nil}})
  (is (= #{:sphere :box :cone} (sut/list-tags))))

(deftest list-tags-returns-empty-set-when-no-tags
  (swap! sm/scene assoc "model-a" {:tags {}})
  (is (= #{} (sut/list-tags))))
