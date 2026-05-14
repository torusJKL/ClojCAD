(ns ClojCAD.demo
  (:require [ClojCAD.kernel.api :as kernel]
            [ClojCAD.scene.manager :as sm]
            [ClojCAD.model.core :as model])
  (:require-macros [ClojCAD.model.defmodel :refer [defmodel]]))

(def params sm/params)

(defmodel sphere [r]
  (kernel/make-sphere r))

(defmodel ^{:opacity 0.3} box [w d h]
  (kernel/make-box w d h))

(defn start-demo! []
  (reset! params {:r 10})
  (sm/show sphere)
  (sm/show box {:w 15 :d 15 :h 15} {:opacity 0.3}))
