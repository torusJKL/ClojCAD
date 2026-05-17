(ns ClojCAD.viewport.export-ui
  (:require [ClojCAD.kernel.api :as kernel]
            [ClojCAD.viewport.loading :as loading]))

(defonce *export-ui (atom nil))
(defonce *export-quality (atom {:max-deviation 0.02}))
(defonce *display-ref (atom nil))

(defn- scene-data []
  (let [^js ns (js* "ClojCAD.scene.manager")]
    {:scene @(.-scene ns)
     :current-model @(.-current-model ns)}))

(defn- close-dropdown! []
  (when-let [ui @*export-ui]
    (set! (.. (:dropdown ui) -style -display) "none")))

(defn- on-outside-click [e]
  (when-let [ui @*export-ui]
    (when (and (not (.contains (:button ui) (.-target e)))
               (not (.contains (:dropdown ui) (.-target e))))
      (close-dropdown!))))

(defn- do-export! [format shape filename opts]
  (loading/show-loading!)
  (js/setTimeout
    (fn []
      (try
        (case format
          "stl" (kernel/export-stl shape filename opts)
          "step" (kernel/export-step shape filename opts))
        (finally
          (loading/hide-loading!))))
    50))

(defn- on-export! [format]
  (close-dropdown!)
  (let [{:keys [scene]} (scene-data)
        visible-entries (->> scene
                             (filter (fn [[_ v]] (:visible? v)))
                             (remove (fn [[_ v]] (nil? (:occt-shape v))))
                             vec)
        visible-shapes (mapv (fn [[_ v]] (:occt-shape v)) visible-entries)]
    (if (empty? visible-shapes)
      (do (js/console.warn "export-ui: no visible models to export")
          (loading/notify! "No visible models to export"))
      (let [opts @*export-quality
            filename (if (= (count visible-shapes) 1)
                       (str (ffirst visible-entries) "." format)
                       (str "export." format))]
        (do-export! format visible-shapes filename opts)))))

(defn- make-dropdown-item [label format]
  (let [item (js/document.createElement "div")]
    (set! (.-textContent item) label)
    (set! (.-className item) "tcv_dropdown-entry")
    (set! (.. item -style -padding) "4px 14px")
    (set! (.. item -style -cursor) "pointer")
    (set! (.. item -style -whiteSpace) "nowrap")
    (.addEventListener item "click" #(on-export! format))
    item))

(defn- download-svg []
  (let [svg (.createElementNS js/document "http://www.w3.org/2000/svg" "svg")]
    (.setAttribute svg "viewBox" "0 0 24 24")
    (.setAttribute svg "width" "16")
    (.setAttribute svg "height" "16")
    (set! (.. svg -style -display) "block")
    (set! (.. svg -style -fill) "var(--tcv-font-color)")
    (set! (.-innerHTML svg)
      (str
        "<path d=\"M12 3c.55 0 1 .45 1 1v8.59l3.29-3.3a1 1 0 0 1 1.42 1.42l-5 5a1 1 0 0 1-1.42 0l-5-5a1 1 0 0 1 1.42-1.42L11 12.59V4c0-.55.45-1 1-1z\"/>"
        "<path d=\"M4 19c0-.55.45-1 1-1h14c.55 0 1 .45 1 1s-.45 1-1 1H5c-.55 0-1-.45-1-1z\"/>"))
    svg))

(defn mount-export-button!
  "Mount the STL/STEP export dropdown button into the three-cad-viewer toolbar." [display]
  (let [toolbar (.. display -cadTool -container)]
    (when-not (nil? toolbar)
      (let [help-btn (.querySelector toolbar ".tcv_button_help")
            ref-node (when help-btn (.closest help-btn ".tcv_tooltip"))
            container (js/document.createElement "div")
            tooltip-wrap (js/document.createElement "span")
            button (js/document.createElement "button")
            dropdown (js/document.createElement "div")]
        (set! (.. container -style -position) "relative")
        (set! (.. container -style -display) "inline-block")
        (set! (.-className tooltip-wrap) "tcv_tooltip")
        (.setAttribute tooltip-wrap "data-tooltip" "Export")
        (.setAttribute tooltip-wrap "data-base-tooltip" "Export")
        (set! (.-className button) "tcv_btn tcv_btn_highlight tcv_round")
        (set! (.. button -style -padding) "0 6px")
        (set! (.. button -style -height) "28px")
        (set! (.. button -style -cursor) "pointer")
        (set! (.. button -style -display) "flex")
        (set! (.. button -style -alignItems) "center")
        (set! (.. button -style -justifyContent) "center")
        (.appendChild button (download-svg))
        (.appendChild tooltip-wrap button)
        (set! (.-className dropdown) "tcv_dropdown-content tcv_round")
        (set! (.. dropdown -style -minWidth) "160px")
        (if ref-node
          (.insertBefore toolbar container ref-node)
          (.appendChild toolbar container))
        (.appendChild container tooltip-wrap)
        (.appendChild container dropdown)
        (.appendChild dropdown (make-dropdown-item "Export STL (.stl)" "stl"))
        (.appendChild dropdown (make-dropdown-item "Export STEP (.step)" "step"))
        (.addEventListener button "click"
          (fn [e]
            (.stopPropagation e)
            (let [dd-style (.. dropdown -style)]
              (if (= (.-display dd-style) "none")
                (set! (.-display dd-style) "block")
                (set! (.-display dd-style) "none")))))
        (reset! *display-ref display)
        (reset! *export-ui {:button button :dropdown dropdown})
        (.addEventListener js/document "click" on-outside-click)
        (set! (.-setExportQuality js/window)
          (fn [deviation]
            (reset! *export-quality {:max-deviation deviation})
            (js/console.log (str "export quality set to max-deviation=" deviation)))))
        (set! (.-setViewerTheme js/window)
          (fn [theme]
            (let [d @*display-ref]
              (when d
                (set! (.. d -container -dataset -theme) theme)
                (set! (.. js/document -body -dataset -theme) theme)
                (set! (.-theme d) theme)
                (js/console.log (str "viewer theme set to " theme)))))))))
