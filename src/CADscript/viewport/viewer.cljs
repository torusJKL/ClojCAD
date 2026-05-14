(ns CADscript.viewport.viewer
  (:require ["three-cad-viewer" :refer (Display Viewer)]))

(defonce *viewer (atom nil))
(defonce *display (atom nil))
(defonce *notify-handler (atom nil))
(defonce *rendered? (atom false))

(defn- notify-callback [change]
  (when-let [handler @*notify-handler]
    (handler change)))

(defn set-notify-handler! [f]
  (reset! *notify-handler f))

(defn- update-size! []
  (when-let [display @*display]
    (let [container (js/document.getElementById "cad-view")
          w (.-clientWidth container)
          h (.-clientHeight container)]
      (.setSizes display #js {:cadWidth (- w 244) :height (- h 40) :treeWidth 240 :treeHeight (min (- h 40) 400) :glass false})
      (when-let [viewer @*viewer]
        (.. viewer -state (set "cadWidth" (- w 244)))
        (.. viewer -state (set "height" (- h 40)))
        (when @*rendered?
          (.resize display))))))

(defn init-viewer! []
  (let [container (js/document.getElementById "cad-view")
        w (.-clientWidth container)
        h (.-clientHeight container)
        display (Display. container
                  #js {:glass false
                       :tools true
                       :cadWidth (- w 244)
                       :height (- h 40)
                       :treeWidth 240
                       :treeHeight (min (- h 40) 400)
                       :theme "dark"})
        viewer (Viewer. display
                  #js {:cadWidth (- w 244) :height (- h 40) :target #js [0 0 0] :up "Z"}
                  notify-callback)]
    (reset! *display display)
    (reset! *viewer viewer)
    (js/window.addEventListener "resize" update-size!)
    viewer))

(defn render-initial! [shapes]
  (when-let [viewer @*viewer]
    (update-size!)
    (.render viewer shapes #js {:edgeColor 0x707070} #js {})
    (reset! *rendered? true)))
