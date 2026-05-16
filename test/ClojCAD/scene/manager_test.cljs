(ns ClojCAD.scene.manager-test
  (:require [cljs.test :refer [deftest is testing use-fixtures]]
            [ClojCAD.scene.manager :as sut]))

(use-fixtures :each
  (fn [f]
    (reset! sut/params {})
    (reset! sut/scene {})
    (reset! sut/current-model nil)
    (f)))

(deftest params-atom-starts-empty
  (is (= {} @sut/params)))

(deftest params-can-be-reset
  (reset! sut/params {:r 10})
  (is (= {:r 10} @sut/params)))

(deftest show-model-updates-scene
  (let [name "test-model"
        scene-entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc name scene-entry)
    (is (contains? @sut/scene name))
    (is (:visible? (get @sut/scene name)))))

(deftest hide-model-sets-visible-false
  (let [name "test-model"
        scene-entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc name scene-entry)
    (let [path (str "/root/" name)]
      (swap! sut/scene assoc-in [name :visible?] false))
    (is (false? (:visible? (get @sut/scene name))))))

(deftest show-model-sets-visible-true
  (let [name "test-model"
        scene-entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? false}]
    (swap! sut/scene assoc name scene-entry)
    (swap! sut/scene assoc-in [name :visible?] true)
    (is (true? (:visible? (get @sut/scene name))))))

(deftest remove-model-dissociates-from-scene
  (let [name "test-model"
        scene-entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc name scene-entry)
    (swap! sut/scene dissoc name)
    (is (not (contains? @sut/scene name)))))

(deftest set-opacity-updates-scene
  (let [name "test-model"
        scene-entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc name scene-entry)
    (swap! sut/scene assoc-in [name :opts :opacity] 0.5)
    (is (= 0.5 (get-in @sut/scene [name :opts :opacity])))))
