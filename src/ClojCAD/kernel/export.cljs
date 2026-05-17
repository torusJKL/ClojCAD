(ns ClojCAD.kernel.export
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.mesh :as mesh]))

(defn- ^js oc []
  @init/oc-instance)

(defn- read-memfs! [path encoding]
  (let [^js fs (.-FS (oc))]
    (.readFile fs path #js {:encoding encoding})))

(defn- unlink-memfs! [path]
  (let [^js fs (.-FS (oc))]
    (try (.unlink fs path) (catch :default _))))

(defn- download-blob! [data filename content-type]
  (let [blob (js/Blob. #js [data] #js {:type content-type})
        url (js/URL.createObjectURL blob)
        a (js/document.createElement "a")]
    (set! (.-href a) url)
    (set! (.-download a) filename)
    (set! (.-style a) "display:none")
    (js/document.body.appendChild a)
    (.click a)
    (js/document.body.removeChild a)
    (js/URL.revokeObjectURL url)))

(defn- invalid-shape? [^js s]
  (or (nil? s) (.IsNull s)))

(defn- face-normal [v0 v1 v2]
  (let [ax (- (aget v1 0) (aget v0 0))
        ay (- (aget v1 1) (aget v0 1))
        az (- (aget v1 2) (aget v0 2))
        bx (- (aget v2 0) (aget v0 0))
        by (- (aget v2 1) (aget v0 1))
        bz (- (aget v2 2) (aget v0 2))
        nx (- (* ay bz) (* az by))
        ny (- (* az bx) (* ax bz))
        nz (- (* ax by) (* ay bx))
        len (Math/hypot nx ny nz)]
    (if (zero? len)
      #js [0.0 0.0 1.0]
      #js [(/ nx len) (/ ny len) (/ nz len)])))

(defn- vertices->float64 [verts i]
  #js [(aget verts (* i 3))
       (aget verts (inc (* i 3)))
       (aget verts (+ 2 (* i 3)))])

(defn- write-binary-stl! [mesh filename]
  (let [indices (:indices mesh)
        verts (:vertices mesh)
        nt (alength indices)
        tri-count (/ nt 3)
        header-size 80
        buf-size (+ header-size 4 (* tri-count 50))
        buf (js/ArrayBuffer. buf-size)
        dv (js/DataView. buf)
        _ (dotimes [i header-size]
            (.setUint8 dv i 0))]
    (.setUint32 dv header-size tri-count true)
    (loop [ti 0
           offset (+ header-size 4)]
      (when (< ti tri-count)
        (let [i0 (aget indices (* ti 3))
              i1 (aget indices (inc (* ti 3)))
              i2 (aget indices (+ 2 (* ti 3)))
              v0 (vertices->float64 verts i0)
              v1 (vertices->float64 verts i1)
              v2 (vertices->float64 verts i2)
              n (face-normal v0 v1 v2)]
          (.setFloat32 dv offset (aget n 0) true)
          (.setFloat32 dv (+ offset 4) (aget n 1) true)
          (.setFloat32 dv (+ offset 8) (aget n 2) true)
          (.setFloat32 dv (+ offset 12) (aget v0 0) true)
          (.setFloat32 dv (+ offset 16) (aget v0 1) true)
          (.setFloat32 dv (+ offset 20) (aget v0 2) true)
          (.setFloat32 dv (+ offset 24) (aget v1 0) true)
          (.setFloat32 dv (+ offset 28) (aget v1 1) true)
          (.setFloat32 dv (+ offset 32) (aget v1 2) true)
          (.setFloat32 dv (+ offset 36) (aget v2 0) true)
          (.setFloat32 dv (+ offset 40) (aget v2 1) true)
          (.setFloat32 dv (+ offset 44) (aget v2 2) true)
          (.setUint16 dv (+ offset 48) 0 true)
          (recur (inc ti) (+ offset 50)))))
    (download-blob! buf filename "model/stl")))

(defn- merge-meshes [meshes]
  (let [total-verts (reduce + 0 (map #(alength (:vertices %)) meshes))
        total-indices (reduce + 0 (map #(alength (:indices %)) meshes))
        result-vertices (js/Float32Array. total-verts)
        result-normals (js/Float32Array. total-verts)
        result-indices (js/Uint32Array. total-indices)
        voffset (atom 0)
        ioffset (atom 0)]
    (doseq [mesh meshes]
      (let [verts (:vertices mesh)
            norms (:normals mesh)
            idxs (:indices mesh)
            nv (alength verts)
            ni (alength idxs)]
        (.set result-vertices verts @voffset)
        (.set result-normals norms @voffset)
        (loop [i 0]
          (when (< i ni)
            (aset result-indices (+ @ioffset i) (+ (aget idxs i) (/ @voffset 3)))
            (recur (inc i))))
        (swap! voffset + nv)
        (swap! ioffset + ni)))
    {:vertices result-vertices
     :normals result-normals
     :indices result-indices
     :obj-vertices result-vertices
     :face-types (js/Int32Array. 0)
     :triangles-per-face (js/Int32Array. 0)
     :edge-types (js/Int32Array. 0)
     :segments-per-edge (js/Int32Array. 0)
     :edges (js/Float32Array. 0)}))

(defn export-stl
  "Export one or more shapes to a binary STL file and trigger a browser download.
   Options may include :max-deviation (default 0.05) to control tessellation quality." ([shape filename]
   (export-stl shape filename nil))
  ([shape filename {:keys [max-deviation]}]
   (let [shapes (if (sequential? shape) shape [shape])]
     (if (some invalid-shape? shapes)
       (js/console.warn "export-stl: invalid shape in input")
       (try
         (let [deviation (or max-deviation 0.05)]
           (if (= (count shapes) 1)
             (let [mesh (mesh/tessellate (first shapes) deviation)]
               (write-binary-stl! mesh filename))
             (let [meshes (mapv #(mesh/tessellate % deviation) shapes)
                   merged (merge-meshes meshes)]
               (write-binary-stl! merged filename))))
         (catch :default e
           (js/console.warn "export-stl failed:" e)))))))

(declare oc-ifselect-retdone)

(defn- transfer-all [^js writer shapes mode progress]
  (loop [[s & rest] shapes]
    (if s
      (let [st (.Transfer_1 writer s mode true progress)]
        (if (not= st (oc-ifselect-retdone))
          (do (js/console.warn "export-step: transfer failed")
              false)
          (recur rest)))
      true)))

(defn- ^js oc-ifselect-retdone []
  (let [^js oc-inst (oc)
        ^js ret-status (.-IFSelect_ReturnStatus oc-inst)]
    (.-IFSelect_RetDone ret-status)))

(defn export-step
  "Export one or more shapes to a STEP file and trigger a browser download.
   Exports using the STEP AP203 'AsIs' mode." ([shape filename]
   (export-step shape filename nil))
  ([shape filename _opts]
   (let [shapes (if (sequential? shape) shape [shape])]
     (if (some invalid-shape? shapes)
       (js/console.warn "export-step: invalid shape in input")
       (let [^js oc-inst (oc)
             ^js writer (js/Reflect.construct (.-STEPControl_Writer_1 oc-inst) #js [])
             ^js step-model-type (.-STEPControl_StepModelType oc-inst)
             mode (.-STEPControl_AsIs step-model-type)
             ^js progress (js/Reflect.construct (.-Message_ProgressRange_1 oc-inst) #js [])]
         (try
           (when (transfer-all writer shapes mode progress)
             (let [memfs-path (str "/" filename)]
               (when (= (.Write writer memfs-path)
                       (oc-ifselect-retdone))
                 (let [data (read-memfs! memfs-path "utf8")]
                   (download-blob! data filename "model/step")))
               (unlink-memfs! memfs-path)))
           (catch :default e
             (js/console.warn "export-step failed:" e))
           (finally
             (.delete writer)
            (.delete progress))))))))
