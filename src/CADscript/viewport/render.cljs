(ns CADscript.viewport.render
  (:require [CADscript.viewport.scene :as vs]
            [CADscript.viewport.mesh-builder :as mb]
            [CADscript.scene.manager :as sm]))

(defn- dispose-object [obj]
  (when (.-geometry obj)
    (.dispose (.-geometry obj)))
  (when (.-material obj)
    (.dispose (.-material obj))))

(defn update-viewport! []
  (let [s @vs/scene
        r @vs/renderer
        c @vs/camera
        mg @vs/models-group]
    (doseq [child (.-children mg)]
      (dispose-object child))
    (doseq [child (.-children mg)]
      (.remove mg child))
    (doseq [[name entry] @sm/scene
            :when (and (:visible? entry true)
                       (some? (:mesh entry))
                       (not (:error entry)))]
      (let [main-mesh (mb/build-mesh (:mesh entry))
            opacity (get-in entry [:opts :opacity])]
        (when opacity
          (set! (.. main-mesh -material -opacity) opacity)
          (set! (.. main-mesh -material -transparent) true))
        (.add mg main-mesh)
        (doseq [[label tag-mesh-data] (:tags entry)
                :when (get-in entry [:tags-visible label] true)]
          (let [tag-mesh (mb/build-mesh tag-mesh-data)]
            (.add mg tag-mesh)))))
    (.render r s c)))

(defn start-loop! []
  (letfn [(loop []
            (js/requestAnimationFrame loop)
            (let [r @vs/renderer
                  s @vs/scene
                  c @vs/camera]
              (when (and r s c)
                (.render r s c))))]
    (loop)))
