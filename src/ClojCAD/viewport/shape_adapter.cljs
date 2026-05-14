(ns ClojCAD.viewport.shape-adapter)

(defn tessellation->shape [{:keys [vertices normals indices edges]}]
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

(defn build-part [model-name shape-data & [opts]]
  (let [mname (name model-name)
        {:keys [color opacity]} opts]
    (make-leaf mname (str "/" mname) shape-data
               (or color 0x4488cc) (or opacity 1.0) #js [1 1])))

(defn build-child-part [model-name tag-label shape-data & [pos]]
  (let [mname (name model-name)
        tname (name tag-label)]
    (make-leaf tname (str "/" mname "/" tname) shape-data
               0x4488cc 1.0 #js [1 1] pos)))

(defn build-shapes-tree [model-name main-part tag-parts]
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
