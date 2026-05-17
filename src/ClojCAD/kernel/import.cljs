(ns ClojCAD.kernel.import
  (:require [ClojCAD.kernel.init :as init]))

(defn- ^js oc []
  @init/oc-instance)

(defn- write-memfs-binary! [^js oc-inst path data]
  (let [^js fs (.-FS oc-inst)]
    (.createDataFile fs "/" path (js/Uint8Array. data) true true)))

(defn- write-memfs-text! [^js oc-inst path text]
  (let [^js fs (.-FS oc-inst)]
    (.createDataFile fs "/" path text true true)))

(defn- unlink-memfs! [^js oc-inst path]
  (let [^js fs (.-FS oc-inst)]
    (try (.unlink fs path) (catch :default _))))

(defn- ensure-solid! [^js oc-inst ^js shape]
  (let [stype (.ShapeType shape)
        ^js shape-enum (.-TopAbs_ShapeEnum oc-inst)
        solid-enum (.-TopAbs_SOLID shape-enum)
        shell-enum (.-TopAbs_SHELL shape-enum)]
    (cond
      (= stype solid-enum)
      shape
      (= stype shell-enum)
      (let [^js topo-cast (.-TopoDS_Cast oc-inst)
            shell (.Shell_1 topo-cast shape)
            ^js maker (js/Reflect.construct (.-BRepBuilderAPI_MakeSolid_1 oc-inst) #js [])]
        (.Add maker shell)
        (let [result (.Solid maker)]
          (.delete maker)
          result))
      :else
      (do (js/console.log "import-stl: shape type" stype "- using as-is")
          shape))))

(defn import-stl
  "Import an STL file (ASCII or binary) from an ArrayBuffer.
   Returns a TopoDS_Shape (converted to a SOLID if needed) or nil on failure."
  [data filename]
  (let [^js oc-inst (oc)
        memfs-path (str "/" filename)]
    (write-memfs-binary! oc-inst memfs-path data)
    (let [^js reader (js/Reflect.construct (.-StlAPI_Reader oc-inst) #js [])
          ^js read-shape (js/Reflect.construct (.-TopoDS_Shape oc-inst) #js [])]
      (try
        (if (.Read_1 reader read-shape filename)
          (ensure-solid! oc-inst read-shape)
          (do (js/console.warn "import-stl: failed to read" filename)
              nil))
        (catch :default e
          (js/console.warn "import-stl: error importing" filename (str e))
          nil)
        (finally
          (.delete reader)
          (unlink-memfs! oc-inst memfs-path))))))

(defn import-step
  "Import a STEP file from a text string.
   Returns a TopoDS_Shape or nil on failure."
  [text filename]
  (let [^js oc-inst (oc)
        memfs-path (str "/" filename)]
    (write-memfs-text! oc-inst memfs-path text)
    (let [^js reader (js/Reflect.construct (.-STEPControl_Reader_1 oc-inst) #js [])
          ^js progress (js/Reflect.construct (.-Message_ProgressRange_1 oc-inst) #js [])
          ^js ret-status (.-IFSelect_ReturnStatus oc-inst)
          ret-done (.-IFSelect_RetDone ret-status)]
      (try
        (if (= (.ReadFile reader memfs-path) ret-done)
          (do (.TransferRoots reader progress)
              (.OneShape reader))
          (do (js/console.warn "import-step: failed to read" filename)
              nil))
        (catch :default e
          (js/console.warn "import-step: error importing" filename e)
          nil)
        (finally
          (.delete reader)
          (.delete progress)
          (unlink-memfs! oc-inst memfs-path))))))
