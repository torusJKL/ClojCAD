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

(deftest show-model-with-tag-map-shows-tag-on-all-models
  (let [entry {:occt-shape nil :mesh nil :tags {:sphere nil} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "model-a" entry "model-b" entry)
    (sut/show-model {:tag :sphere})
    (is (true? (get-in @sut/scene ["model-a" :tags-visible "sphere"])))
    (is (true? (get-in @sut/scene ["model-b" :tags-visible "sphere"])))))

(deftest show-model-with-tag-map-skips-models-without-tag
  (let [entry-a {:occt-shape nil :mesh nil :tags {:sphere nil} :tags-visible {} :opts {} :visible? true}
        entry-b {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "model-a" entry-a "model-b" entry-b)
    (sut/show-model {:tag :sphere})
    (is (nil? (get-in @sut/scene ["model-b" :tags-visible "sphere"])))))

(deftest show-model-with-tag-and-model-targets-specific-model
  (let [entry {:occt-shape nil :mesh nil :tags {:sphere nil} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "model-a" entry "model-b" entry)
    (sut/show-model {:tag :sphere :model 'model-a})
    (is (true? (get-in @sut/scene ["model-a" :tags-visible "sphere"])))
    (is (nil? (get-in @sut/scene ["model-b" :tags-visible "sphere"])))))

(deftest show-model-symbol-shows-whole-model-unchanged
  (let [entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? false}]
    (swap! sut/scene assoc "test-model" entry)
    (sut/show-model 'test-model)
    (is (true? (:visible? (get @sut/scene "test-model"))))))

(deftest hide-model-with-tag-map-hides-tag-on-all-models
  (let [entry {:occt-shape nil :mesh nil :tags {:sphere nil} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "model-a" entry "model-b" entry)
    (sut/hide-model {:tag :sphere})
    (is (false? (get-in @sut/scene ["model-a" :tags-visible "sphere"])))
    (is (false? (get-in @sut/scene ["model-b" :tags-visible "sphere"])))))

(deftest hide-model-symbol-hides-whole-model-unchanged
  (let [entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "test-model" entry)
    (sut/hide-model 'test-model)
    (is (false? (:visible? (get @sut/scene "test-model"))))))

(deftest toggle-model-inverts-tag-visibility
  (let [entry {:occt-shape nil :mesh nil :tags {:sphere nil} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "test-model" entry)
    (sut/toggle-model {:tag :sphere})
    (is (false? (get-in @sut/scene ["test-model" :tags-visible "sphere"])))
    (sut/toggle-model {:tag :sphere})
    (is (true? (get-in @sut/scene ["test-model" :tags-visible "sphere"])))))

(deftest toggle-model-inverts-whole-model-visibility
  (let [entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "test-model" entry)
    (sut/toggle-model {:model 'test-model})
    (is (false? (:visible? (get @sut/scene "test-model"))))))

(deftest show-all-makes-all-models-visible
  (let [entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? false}]
    (swap! sut/scene assoc "model-a" entry "model-b" entry)
    (sut/show-all)
    (is (true? (:visible? (get @sut/scene "model-a"))))
    (is (true? (:visible? (get @sut/scene "model-b"))))))

(deftest hide-all-makes-all-models-hidden
  (let [entry {:occt-shape nil :mesh nil :tags {} :tags-visible {} :opts {} :visible? true}]
    (swap! sut/scene assoc "model-a" entry "model-b" entry)
    (sut/hide-all)
    (is (false? (:visible? (get @sut/scene "model-a"))))
    (is (false? (:visible? (get @sut/scene "model-b"))))))
