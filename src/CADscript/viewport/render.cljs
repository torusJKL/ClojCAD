(ns CADscript.viewport.render
  (:require [CADscript.viewport.scene :as vs]
            [CADscript.viewport.mesh-builder :as mb]
            [CADscript.scene.manager :as sm]))

(defn- dispose-object [obj]
  (when (.-geometry obj)
    (.dispose (.-geometry obj)))
  (when (.-material obj)
    (.dispose (.-material obj))))

(defn- add-mesh-to-scene [mg {:keys [face-mesh edge-mesh]} opts]
  (when face-mesh
    (when-let [opacity (:opacity opts)]
      (set! (.. face-mesh -material -opacity) opacity)
      (set! (.. face-mesh -material -transparent) true))
    (.add mg face-mesh))
  (when edge-mesh
    (.add mg edge-mesh)))

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
      (let [result (mb/build-mesh (:mesh entry))]
        (add-mesh-to-scene mg result (:opts entry))
        (doseq [[label tag-mesh-data] (:tags entry)
                :when (get-in entry [:tags-visible label] true)]
          (let [tag-result (mb/build-mesh tag-mesh-data)]
            (add-mesh-to-scene mg tag-result {})))))
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
