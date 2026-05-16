(ns ClojCAD.kernel.export-test
  (:require [cljs.test :refer [deftest is testing]]
            [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.primitives :as prim]
            [ClojCAD.kernel.export :as sut]))

(defn- setup-dom-mocks []
  (let [body-children (atom [])
        mock-anchor (atom nil)]
    (set! js/document
      #js {:createElement
           (fn [tag]
             (if (= tag "a")
               (let [el #js {:href ""
                             :download ""
                             :style #js {}
                             :click (fn [])
                             :tag tag}]
                 (reset! mock-anchor el)
                 el)
               (let [el #js {:tag tag
                             :appendChild (fn [child] child)}]
                 el)))
            :body
            #js {:appendChild (fn [el]
                                (swap! body-children conj el)
                                el)
                 :removeChild (fn [el] el)}})
    (set! js/URL
      #js {:createObjectURL (fn [_] "blob:mock")
           :revokeObjectURL (fn [_])})
    (set! js/Blob
      (fn [parts opts] #js {:parts parts :type (.-type opts)}))
    #js {:body-children body-children
         :mock-anchor mock-anchor}))

(deftest export-stl-with-valid-shape
  (when-some [oc @init/oc-instance]
    (let [mocks (setup-dom-mocks)
          shape (prim/make-sphere 10)]
      (testing "export-stl does not throw with valid shape"
        (is (nil? (sut/export-stl shape "test.stl"))))
      (testing "export-step does not throw with valid shape"
        (is (nil? (sut/export-step shape "test.step")))))))

(deftest export-step-with-multiple-shapes
  (when-some [oc @init/oc-instance]
    (let [mocks (setup-dom-mocks)
          a (prim/make-box 10 10 10)
          b (prim/translate (prim/make-box 10 10 10) 20 0 0)]
      (testing "export-step accepts a vector of shapes"
        (is (nil? (sut/export-step [a b] "multi.step")))))))

(deftest export-stl-with-invalid-shape-nil
  (let [warn-calls (atom [])
        orig-warn js/console.warn]
    (set! js/console.warn (fn [& args] (swap! warn-calls conj (vec args))))
    (sut/export-stl nil "nil.stl")
    (is (pos? (count @warn-calls)))
    (is (re-find #"(?i)invalid" (str (first @warn-calls))))
    (set! js/console.warn orig-warn)))

(deftest export-step-with-invalid-shape-nil
  (let [warn-calls (atom [])
        orig-warn js/console.warn]
    (set! js/console.warn (fn [& args] (swap! warn-calls conj (vec args))))
    (sut/export-step nil "nil.step")
    (is (pos? (count @warn-calls)))
    (is (re-find #"(?i)invalid" (str (first @warn-calls))))
    (set! js/console.warn orig-warn)))
