(ns CADscript.viewport.mesh-builder
  (:require ["three" :as three]))

(defn build-mesh [{:keys [vertices indices]}]
  (let [geometry (three/BufferGeometry.)
        positions (js/Float32Array. (clj->js vertices))
        idx (js/Uint32Array. (clj->js indices))]
    (.setAttribute geometry "position" (three/BufferAttribute. positions 3))
    (.setIndex geometry (three/BufferAttribute. idx 1))
    (let [material (three/MeshStandardMaterial.
                    #js {:color 0x4488cc
                         :side (.-DoubleSide three)
                         :flatShading true})]
      (three/Mesh. geometry material))))
