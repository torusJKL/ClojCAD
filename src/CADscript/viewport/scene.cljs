(ns CADscript.viewport.scene
  (:require ["three" :as three]))

(defonce renderer (atom nil))
(defonce camera (atom nil))
(defonce scene (atom nil))
(defonce models-group (atom nil))

(defn init-viewport! []
  (let [container (js/document.getElementById "viewport")
        w (.-clientWidth container)
        h (.-clientHeight container)
        s (three/Scene.)
        c (three/PerspectiveCamera. 45 (/ w h) 0.1 1000)
        r (three/WebGLRenderer. #js {:antialias true})
        mg (three/Group.)]
    (set! (.. s -background) (three/Color. 0x222222))
    (set! (.-x (.-position c)) 30)
    (set! (.-y (.-position c)) 30)
    (set! (.-z (.-position c)) 30)
    (.lookAt c (three/Vector3. 0 0 0))
    (.add s (three/AmbientLight. 0xffffff 0.6))
    (.add s (three/DirectionalLight. 0xffffff 1.0))
    (.add s (three/GridHelper. 20 20))
    (.add s (three/AxesHelper. 10))
    (.add s mg)
    (.setSize r w h)
    (.appendChild container (.-domElement r))
    (reset! models-group mg)
    (reset! scene s)
    (reset! camera c)
    (reset! renderer r)
    (js/window.addEventListener "resize"
      (fn []
        (let [w2 (.-clientWidth container)
              h2 (.-clientHeight container)]
          (.setSize r w2 h2)
          (set! (.-aspect c) (/ w2 h2))
          (.updateProjectionMatrix c))))
    r))
