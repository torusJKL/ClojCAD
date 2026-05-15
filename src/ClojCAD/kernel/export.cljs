(ns ClojCAD.kernel.export
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.mesh :as mesh]))

(defn- oc []
  @init/oc-instance)

(defn- read-memfs! [path encoding]
  (let [fs (.-FS (oc))]
    (.readFile fs path #js {:encoding encoding})))

(defn- unlink-memfs! [path]
  (let [fs (.-FS (oc))]
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

(defn- invalid-shape? [s]
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

(defn export-stl
  ([shape filename]
   (export-stl shape filename nil))
  ([shape filename {:keys [max-deviation]}]
   (if (invalid-shape? shape)
     (js/console.warn "export-stl: invalid shape")
     (try
       (let [deviation (or max-deviation 0.05)
             mesh (mesh/tessellate shape deviation)]
         (write-binary-stl! mesh filename))
       (catch :default e
         (js/console.warn "export-stl failed:" e))))))

(defn- transfer-all [writer shapes mode progress]
  (loop [[s & rest] shapes]
    (if s
      (let [st (.Transfer_1 writer s mode true progress)]
        (if (not= st (.. (oc) -IFSelect_ReturnStatus -IFSelect_RetDone))
          (do (js/console.warn "export-step: transfer failed")
              false)
          (recur rest)))
      true)))

(defn- oc-ifselect-retdone []
  (.. (oc) -IFSelect_ReturnStatus -IFSelect_RetDone))

(defn export-step
  ([shape filename]
   (export-step shape filename nil))
  ([shape filename _opts]
   (let [shapes (if (sequential? shape) shape [shape])]
     (if (some invalid-shape? shapes)
       (js/console.warn "export-step: invalid shape in input")
       (let [oc-inst (oc)
             writer (js/Reflect.construct (.-STEPControl_Writer_1 oc-inst) #js [])
             mode (.. oc-inst -STEPControl_StepModelType -STEPControl_AsIs)
             progress (js/Reflect.construct (.-Message_ProgressRange_1 oc-inst) #js [])]
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
