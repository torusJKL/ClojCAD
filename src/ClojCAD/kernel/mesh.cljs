(ns ClojCAD.kernel.mesh)

(defn- ^js oc []
  (let [^js init-ns (js* "ClojCAD.kernel.init")]
    @(.-oc_instance init-ns)))

(defn- empty-result []
  {:vertices (js/Float32Array. 0) :normals (js/Float32Array. 0)
   :indices (js/Uint32Array. 0) :edges (js/Float32Array. 0)
   :obj-vertices (js/Float32Array. 0) :face-types (js/Int32Array. 0)
   :edge-types (js/Int32Array. 0) :triangles-per-face (js/Int32Array. 0)
   :segments-per-edge (js/Int32Array. 0)})

(defn- extract-edges [shape maxDeviation]
  (let [^js oc-inst (oc)
        ^js shape-enum (.-TopAbs_ShapeEnum oc-inst)
        explorer (js/Reflect.construct
                   (.-TopExp_Explorer_2 oc-inst)
                   #js [shape
                        (.-TopAbs_EDGE shape-enum)
                        (.-TopAbs_SHAPE shape-enum)])
        out #js[] edge-types #js[] segments-per-edge #js[]]
    (while (.More explorer)
      (let [edge (.Value explorer)]
        (try
          (let [^js cast (.-TopoDS_Cast oc-inst)
                ^js my-edge (.Edge_1 cast edge)
                ^js adaptor (js/Reflect.construct (.-BRepAdaptor_Curve_2 oc-inst) #js [my-edge])
                curve-type (.GetType adaptor)
                ^js sampler (js/Reflect.construct (.-GCPnts_TangentialDeflection_2 oc-inst) #js [adaptor maxDeviation 0.1 2 1.0e-9 1.0e-7])
                np (.NbPoints sampler)]
            (.push edge-types curve-type)
            (.push segments-per-edge (dec np))
            (when (pos? np)
              (loop [pi 1 prev nil]
                (when (<= pi np)
                  (let [pt (.Value sampler pi)]
                    (when prev
                      (.push out (.X prev)) (.push out (.Y prev)) (.push out (.Z prev))
                      (.push out (.X pt)) (.push out (.Y pt)) (.push out (.Z pt)))
                    (recur (inc pi) pt))))))
          (catch :default _))
        (.Next explorer)))
    {:points out :edge-types edge-types :segments-per-edge segments-per-edge}))

(defn- extract-faces [shape]
  (let [^js oc-inst (oc)
        ^js shape-enum (.-TopAbs_ShapeEnum oc-inst)
        ^js orientation (.-TopAbs_Orientation oc-inst)
        fexp (js/Reflect.construct
               (.-TopExp_Explorer_2 oc-inst)
               #js [shape
                    (.-TopAbs_FACE shape-enum)
                    (.-TopAbs_SHAPE shape-enum)])
        all-verts #js[] all-norms #js[] all-idxs #js[]
        all-obj-verts #js[] face-types #js[] triangles-per-face #js[]
        offset (atom 0)
        ^js cast (.-TopoDS_Cast oc-inst)
        is-forward (.-TopAbs_FORWARD orientation)]
    (while (.More fexp)
      (let [raw-face (.Value fexp)]
        (try
          (let [^js face (.Face_1 cast raw-face)
                ^js loc (js/Reflect.construct (.-TopLoc_Location_1 oc-inst) #js [])
                ^js tri-handle (oc-inst.BRep_Tool.Triangulation face loc 0)
                rev? (not= (.Orientation_1 face) is-forward)
                trsf (.Transformation loc)]
            (when-not (.IsNull tri-handle)
              (let [^js tri (.get tri-handle)
                    nn (.NbNodes tri)
                    nt (.NbTriangles tri)]
                (loop [i 1]
                  (when (<= i nn)
                    (let [^js node (.Node tri i)
                          ^js p (.Transformed node trsf)]
                      (.push all-verts (.X p)) (.push all-verts (.Y p)) (.push all-verts (.Z p))
                      (.push all-obj-verts (.X p)) (.push all-obj-verts (.Y p)) (.push all-obj-verts (.Z p))
                      (recur (inc i)))))
                (when-not (.HasNormals tri) (.ComputeNormals tri))
                (loop [i 1]
                  (when (<= i nn)
                    (let [^js n (.Normal_1 tri i)
                          ^js d (.Transformed n trsf)]
                      (.push all-norms (.X d)) (.push all-norms (.Y d)) (.push all-norms (.Z d))
                      (recur (inc i)))))
                (loop [nti 1]
                  (when (<= nti nt)
                    (let [^js t (.Triangle tri nti)
                          n1 (.Value t 1) n2 (.Value t 2) n3 (.Value t 3)]
                      (if rev?
                        (do (.push all-idxs (+ n2 (dec @offset)))
                            (.push all-idxs (+ n1 (dec @offset)))
                            (.push all-idxs (+ n3 (dec @offset))))
                        (do (.push all-idxs (+ n1 (dec @offset)))
                            (.push all-idxs (+ n2 (dec @offset)))
                            (.push all-idxs (+ n3 (dec @offset)))))
                      (recur (inc nti)))))
                (swap! offset + nn))))
          (catch :default _))
        (.Next fexp)))
    {:vertices all-verts :normals all-norms :indices all-idxs
     :obj-vertices all-obj-verts :face-types face-types
     :triangles-per-face triangles-per-face}))

(defn tessellate
  "Tessellate a shape into a mesh for rendering or export.
   Optional maxDeviation parameter (default 0.1) controls the tessellation quality.
   Accepts either a TopoDS_Shape or a map with a :shape key.
   Returns a map with :vertices, :normals, :indices, :edges, and other mesh data." ([x] (tessellate x 0.1))
  ([x maxDeviation]
   (let [shape (if (map? x) (:shape x) x)]
     (try
       (let [^js oc-inst (oc)]
         (js/Reflect.construct (.-BRepMesh_IncrementalMesh_2 oc-inst)
           #js [shape maxDeviation false (* maxDeviation 5) false])
          (let [{:keys [vertices normals indices obj-vertices face-types triangles-per-face]} (extract-faces shape)
                {:keys [points edge-types segments-per-edge]} (extract-edges shape maxDeviation)]
            {:vertices (js/Float32Array.from vertices)
            :normals (js/Float32Array.from normals)
            :indices (js/Uint32Array.from indices)
            :edges (if (pos? (.-length points))
                    (js/Float32Array.from points)
                    (js/Float32Array. 0))
            :obj-vertices (js/Float32Array.from obj-vertices)
            :face-types (js/Int32Array.from face-types)
            :edge-types (js/Int32Array.from edge-types)
            :triangles-per-face (js/Int32Array.from triangles-per-face)
            :segments-per-edge (js/Int32Array.from segments-per-edge)}))
       (catch :default e
         (js/console.warn "OCCT tessellation failed:" (.-message e))
         (empty-result))))))
