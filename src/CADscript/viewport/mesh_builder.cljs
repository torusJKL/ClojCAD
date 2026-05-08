(ns CADscript.viewport.mesh-builder
  (:require ["three" :as three]))

(defn build-mesh [{:keys [vertices normals indices edges]}]
  (let [geometry (three/BufferGeometry.)
        positions (js/Float32Array.from vertices)
        idx (js/Uint32Array.from indices)]
    (.setAttribute geometry "position" (three/BufferAttribute. positions 3))
    (.setIndex geometry (three/BufferAttribute. idx 1))
    (when normals
      (let [norms (js/Float32Array.from normals)]
        (.setAttribute geometry "normal" (three/BufferAttribute. norms 3))))
    (let [material (three/MeshStandardMaterial.
                    #js {:color 0x4488cc
                         :side (.-DoubleSide three)
                         :flatShading true})
          face-mesh (three/Mesh. geometry material)
          edge-mesh (when (and edges (pos? (.-length edges)))
                      (let [eg (three/BufferGeometry.)
                            epos (js/Float32Array.from edges)]
                        (.setAttribute eg "position" (three/BufferAttribute. epos 3))
                        (three/LineSegments. eg (three/LineBasicMaterial.
                                                  #js {:color 0x707070}))))]
      {:face-mesh face-mesh :edge-mesh edge-mesh})))
