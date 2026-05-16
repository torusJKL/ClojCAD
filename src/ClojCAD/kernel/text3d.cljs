(ns ClojCAD.kernel.text3d
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.font :as font]
            [ClojCAD.kernel.primitives :as primitives]
            [ClojCAD.kernel.lifecycle :as lifecycle]))

(defn- oc-module [] @init/oc-instance)

(defn- pnt [x y]
  (let [oc-m (oc-module)]
    (js/Reflect.construct (.-gp_Pnt_3 oc-m) #js [x y 0])))

(defn- line-edge [p1 p2]
  (let [oc-m (oc-module)
        maker (js/Reflect.construct (.-GC_MakeSegment_1 oc-m) #js [p1 p2])
        seg (.Value maker)
        ptr (.get seg)
        h-curve (new (.-Handle_Geom_Curve_2 oc-m) ptr)
        builder (js/Reflect.construct (.-BRepBuilderAPI_MakeEdge_24 oc-m) #js [h-curve])
        edge (.Edge builder)]
    (.delete builder)
    edge))

(defn- quadratic-edge [p1 cp p2]
  (let [oc-m (oc-module)
        pt-list (js/Reflect.construct (.-TColgp_Array1OfPnt_2 oc-m) #js [1 3])
        _ (.SetValue pt-list 1 p1)
        _ (.SetValue pt-list 2 cp)
        _ (.SetValue pt-list 3 p2)
        curve (new (.-Geom_BezierCurve_1 oc-m) pt-list)
        h-curve (new (.-Handle_Geom_Curve_2 oc-m) curve)
        builder (js/Reflect.construct (.-BRepBuilderAPI_MakeEdge_24 oc-m) #js [h-curve])
        edge (.Edge builder)]
    (.delete builder)
    edge))

(defn- cubic-edge [p1 cp1 cp2 p2]
  (let [oc-m (oc-module)
        pt-list (js/Reflect.construct (.-TColgp_Array1OfPnt_2 oc-m) #js [1 4])
        _ (.SetValue pt-list 1 p1)
        _ (.SetValue pt-list 2 cp1)
        _ (.SetValue pt-list 3 cp2)
        _ (.SetValue pt-list 4 p2)
        curve (new (.-Geom_BezierCurve_1 oc-m) pt-list)
        h-curve (new (.-Handle_Geom_Curve_2 oc-m) curve)
        builder (js/Reflect.construct (.-BRepBuilderAPI_MakeEdge_24 oc-m) #js [h-curve])
        edge (.Edge builder)]
    (.delete builder)
    edge))

(defn- add-edge-to-wire [wire-builder edge]
  (let [oc-m (oc-module)
        inner-builder (js/Reflect.construct (.-BRepBuilderAPI_MakeWire_2 oc-m) #js [edge])
        inner-wire (.Wire inner-builder)]
    (.delete inner-builder)
    (.Add_2 wire-builder inner-wire)))

(defn- make-face-from-wire [wire]
  (let [oc-m (oc-module)
        builder (js/Reflect.construct (.-BRepBuilderAPI_MakeFace_15 oc-m) #js [wire false])
        face (.Face builder)]
    (.delete builder)
    face))

(defn- add-hole-to-face [face wire]
  (let [oc-m (oc-module)
        builder (js/Reflect.construct (.-BRepBuilderAPI_MakeFace_22 oc-m) #js [face wire])
        result (.Face builder)]
    (.delete builder)
    result))

(defn- close-current-contour! [text-faces cur-wire]
  (when-let [w @cur-wire]
    (try
      (let [wire (.Wire w)]
        (if (empty? @text-faces)
          (let [face (make-face-from-wire wire)]
            (swap! text-faces conj face))
          (let [last-face (peek @text-faces)
                new-face (add-hole-to-face last-face wire)]
            (swap! text-faces conj new-face))))
      (catch js/Error e
        (js/console.warn "Could not close contour:" e)))))

(defn commands->face [commands]
  (let [oc-m (oc-module)
        make-wire (.-BRepBuilderAPI_MakeWire_1 oc-m)
        text-faces (atom [])
        first-point (atom nil)
        last-point (atom nil)
        cur-wire (atom nil)]
    (doseq [cmd commands]
      (let [t (.-type cmd)]
        (case t
        "M"
        (do
          (close-current-contour! text-faces cur-wire)
          (reset! first-point (pnt (.-x cmd) (.-y cmd)))
          (reset! last-point @first-point)
          (reset! cur-wire (js/Reflect.construct make-wire #js [])))
        "L"
        (let [next-pt (pnt (.-x cmd) (.-y cmd))]
          (when (not (and (= (.X @last-point) (.X next-pt))
                          (= (.Y @last-point) (.Y next-pt))))
            (let [edge (line-edge @last-point next-pt)]
              (add-edge-to-wire @cur-wire edge))
            (reset! last-point next-pt)))
        "Q"
        (let [cp (pnt (.-x1 cmd) (.-y1 cmd))
              next-pt (pnt (.-x cmd) (.-y cmd))
              edge (quadratic-edge @last-point cp next-pt)]
          (add-edge-to-wire @cur-wire edge)
          (reset! last-point next-pt))
        "C"
        (let [cp1 (pnt (.-x1 cmd) (.-y1 cmd))
              cp2 (pnt (.-x2 cmd) (.-y2 cmd))
              next-pt (pnt (.-x cmd) (.-y cmd))
              edge (cubic-edge @last-point cp1 cp2 next-pt)]
          (add-edge-to-wire @cur-wire edge)
          (reset! last-point next-pt))
        "Z"
        (close-current-contour! text-faces cur-wire)
        nil)))
    (close-current-contour! text-faces cur-wire)
    (peek @text-faces)))

(defn text3d
  [text size & {:keys [font height] :or {font "Cousine" height 0.15}}]
  (let [font-obj (font/lookup-font font)]
    (if font-obj
      (let [path (.getPath font-obj text 0 0 size)
            commands (.-commands path)
            face (commands->face commands)]
        (if face
          (let [_ (lifecycle/track face)]
            (if (zero? height)
              face
              (let [extruded (primitives/extrude face [0 0 (* height size)])
                    rotated (primitives/rotate extruded 1 0 0 -90)]
                rotated)))
          (do
            (js/console.warn "Could not generate face for text:" text)
            nil)))
      (do
        (js/console.warn "Font not found:" font)
        nil))))
