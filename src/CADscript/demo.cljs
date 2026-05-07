(ns CADscript.demo
  (:require [CADscript.kernel.api :as kernel]
            [CADscript.scene.manager :as sm]
            [CADscript.model.core :as model])
  (:require-macros [CADscript.model.defmodel :refer [defmodel]]))

(def params sm/params)

(defmodel sphere [r]
  (kernel/make-sphere r))

(defmodel ^{:opacity 0.3} box [w d h]
  (kernel/make-box w d h))

(defn start-demo! []
  (reset! params {:r 10})
  (sm/show sphere)
  (sm/show box {:w 15 :d 15 :h 15} {:opacity 0.3}))
