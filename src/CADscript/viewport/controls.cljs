(ns CADscript.viewport.controls
  (:require ["three/examples/jsm/controls/OrbitControls" :refer (OrbitControls)]
            [CADscript.viewport.scene :as vs]))

(defonce controls (atom nil))

(defn init-controls! []
  (let [c (OrbitControls. @vs/camera (.-domElement @vs/renderer))]
    (.update c)
    (reset! controls c)
    c))
