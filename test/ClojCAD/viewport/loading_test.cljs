(ns ClojCAD.viewport.loading-test
  (:require [cljs.test :refer [deftest is use-fixtures]]
            [ClojCAD.viewport.loading :as sut]))

(use-fixtures :each {:before (fn []
                               (reset! sut/*injected-style false)
                               (reset! sut/*overlay nil))})

(defn- setup-dom []
  (let [children (array)
        body-el #js {:children children
                     :tag "body"}
        _ (set! (.-appendChild body-el)
           (fn [child]
             (.push children child)
             (set! (.-parentNode child) body-el)
             child))
        _ (set! (.-removeChild body-el)
           (fn [child]
             (let [idx (.indexOf children child)]
               (when (>= idx 0)
                 (.splice children idx 1)))
             (set! (.-parentNode child) nil)
             child))
        head-children (array)
        head-el #js {:children head-children :tag "head"}
        _ (set! (.-appendChild head-el)
           (fn [child]
             (.push head-children child)
             child))]
    (set! js/document
      #js {:body body-el
           :head head-el
           :createElement (fn [tag]
                            #js {:tag tag
                                 :style #js {}
                                 :appendChild (fn [child] child)
                                 :parentNode nil})})
    (set! js/console.warn (fn [& _]))
    body-el))

(deftest show-loading-creates-overlay
  (let [body (setup-dom)]
    (sut/show-loading!)
    (is (= 1 (.-length (.-children body))))
    (is (= "div" (.-tag (aget (.-children body) 0))))))

(deftest show-loading-is-idempotent
  (let [body (setup-dom)]
    (sut/show-loading!)
    (sut/show-loading!)
    (is (= 1 (.-length (.-children body))))))

(deftest hide-loading-removes-overlay
  (let [body (setup-dom)]
    (sut/show-loading!)
    (sut/hide-loading!)
    (is (zero? (.-length (.-children body))))))

(deftest hide-loading-is-safe-when-no-overlay
  (setup-dom)
  (sut/hide-loading!)
  (is true "no exception thrown"))

(deftest notify-creates-toast
  (let [body (setup-dom)]
    (sut/notify! "test message")
    (is (= 1 (.-length (.-children body))))))
