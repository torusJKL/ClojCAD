(ns ClojCAD.viewport.shape-adapter
  (:require [ClojCAD.viewport.config :as cfg]))

(defn tessellation->shape
  "Convert a ClojCAD tessellation map (with :vertices, :normals, :indices, :edges)
   into a plain JS object suitable for three-cad-viewer." [{:keys [vertices normals indices edges]}]
  (let [s (js-obj)]
    (aset s "vertices" vertices)
    (aset s "triangles" indices)
    (aset s "normals" normals)
    (when (and edges (pos? (.-length edges)))
      (aset s "edges" edges))
    s))

(defn- make-leaf [name id shape-data color opacity state & [pos]]
  (let [leaf (js-obj)
        [x y z] (or pos [0 0 0])]
    (aset leaf "name" name)
    (aset leaf "id" id)
    (aset leaf "type" "shapes")
    (aset leaf "subtype" "solid")
    (aset leaf "shape" (tessellation->shape shape-data))
    (aset leaf "color" color)
    (aset leaf "alpha" opacity)
    (aset leaf "state" state)
    (aset leaf "loc" #js [#js [x y z] #js [0 0 0 1]])
    leaf))

(defn build-part
  "Build a single solid part JS object from model-name and tessellation data.
   Options may include :color and :opacity." [model-name shape-data & [opts]]
  (let [mname (name model-name)
        {:keys [color opacity]} opts]
    (make-leaf mname (str "/" mname) shape-data
               (or color (cfg/get-default-shape-color)) (or opacity 1.0) #js [1 1])))

(defn build-child-part
  "Build a tagged child part JS object. The child is positioned at an optional
   [x y z] location. Options may include :color and :opacity." ([model-name tag-label shape-data]
   (build-child-part model-name tag-label shape-data nil nil))
  ([model-name tag-label shape-data pos]
   (build-child-part model-name tag-label shape-data pos nil))
  ([model-name tag-label shape-data pos opts]
   (let [mname (name model-name)
         tname (name tag-label)
         {:keys [color opacity]} (or opts {})]
     (make-leaf tname (str "/" mname "/" tname) shape-data
                (or color (cfg/get-default-shape-color))
                (or opacity 1.0)
                #js [1 1] pos))))

(defn build-shapes-tree
  "Build a shapes tree JS object combining a main body part with tagged child parts
   for rendering in three-cad-viewer." [model-name main-part tag-parts]
  (let [mname (name model-name)
        tree (js-obj)
        parts-arr (array)]
    ;; Give main part a unique name under the tree to avoid id collision
    (aset main-part "name" (str mname "-body"))
    (aset main-part "id" (str "/" mname "/" mname "-body"))
    (.push parts-arr main-part)
    (doseq [p tag-parts]
      (.push parts-arr p))
    (aset tree "name" mname)
    (aset tree "id" (str "/" mname))
    (aset tree "type" "shapes")
    (aset tree "subtype" "solid")
    (aset tree "state" #js [1 1])
    (aset tree "loc" #js [#js [0 0 0] #js [0 0 0 1]])
    (aset tree "parts" parts-arr)
    tree))
