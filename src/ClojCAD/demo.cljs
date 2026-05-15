(ns ClojCAD.demo
  (:require [ClojCAD.kernel.api :as kernel]
            [ClojCAD.kernel.booleans :as booleans]
            [ClojCAD.scene.manager :as sm]
            [ClojCAD.model.core :as model])
  (:require-macros [ClojCAD.model.defmodel :refer [defmodel]]))

(def params sm/params)

(defn- centered-cyl [r h]
  (kernel/translate (kernel/make-cylinder r h) 0 0 (* -0.5 h)))

(defmodel ^{:opacity 0.7} boolean-bench [r]
  (let [;; Step 1: FUSE two overlapping spheres into a peanut shape
        a (kernel/make-sphere r)
        b (kernel/translate (kernel/make-sphere r) (* 1.6 r) 0 0)
        fused (booleans/fuse a b)

        ;; Step 2: COMMON with a box to flatten into a capsule
        bx (kernel/translate (kernel/make-box (* 2.2 r) (* 1.8 r) (* 1.8 r))
              (* -0.3 r) (* -0.9 r) (* -0.9 r))
        capsule (booleans/common fused bx)

        ;; Step 3: CUT three perpendicular holes through center
        hr (* 0.3 r)
        hh (* 6 r)
        cx (* 0.8 r)     ;; center of the shape
        top-hole (kernel/translate (centered-cyl hr hh) cx 0 0)
        side-hole (-> (centered-cyl hr hh)
                      (kernel/rotate 0 1 0 90)
                      (kernel/translate cx 0 0))
        front-hole (-> (centered-cyl hr hh)
                       (kernel/rotate 1 0 0 90)
                       (kernel/translate cx 0 0))]
    (booleans/cut capsule top-hole side-hole front-hole)))

(defn start-demo! []
  (reset! params {:r 10})
  (sm/show boolean-bench {:r 8}))
