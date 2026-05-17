(ns ClojCAD.viewport.import-ui
  (:require [ClojCAD.kernel.api :as kernel]
            [ClojCAD.viewport.shape-adapter :as sa]
            [ClojCAD.viewport.loading :as loading]))

(defonce *file-input (atom nil))

(defn- read-file-as-arraybuffer! [file]
  (js/Promise.
    (fn [resolve reject]
      (let [reader (js/FileReader.)]
        (set! (.-onload reader) #(resolve (.-result (.-target %))))
        (set! (.-onerror reader) #(reject (.-error %)))
        (.readAsArrayBuffer reader file)))))

(defn- read-file-as-text! [file]
  (js/Promise.
    (fn [resolve reject]
      (let [reader (js/FileReader.)]
        (set! (.-onload reader) #(resolve (.-result (.-target %))))
        (set! (.-onerror reader) #(reject (.-error %)))
        (.readAsText reader file)))))

(defn- ^js sm-ns [] (js* "ClojCAD.scene.manager"))
(defn- ^js vw-ns [] (js* "ClojCAD.viewport.viewer"))

(defn- get-viewer []
  (when-let [^js ns (vw-ns)]
    (when-let [viewer-atom (.-*viewer ns)]
      @viewer-atom)))

(defn- add-to-scene! [name-str shape mesh]
  (let [^js ns (sm-ns)]
    (swap! (.-scene ns) assoc name-str
      {:occt-shape shape
       :mesh mesh
       :tags {}
       :tags-visible {}
       :opts {}
       :visible? true})
    (reset! (.-current-model ns) name-str)))

(defn- import-stl-file! [file data]
  (let [shape (kernel/import-stl data (.-name file))]
    (if shape
      (let [name-str (.-name file)
            mesh (kernel/tessellate shape)
            nv (.-length (:vertices mesh))
            ni (.-length (:indices mesh))
            ^js viewer (get-viewer)
            part (sa/build-part name-str mesh)]
        (js/console.log "import-stl: mesh has" nv "vertices," ni "indices")
        (add-to-scene! name-str shape mesh)
        (when viewer
          (.addPart viewer "/root" part)
          (.presetCamera viewer "iso"))
        (js/console.log (str "imported: " name-str)))
      (do (js/console.warn "import-stl: failed to import" (.-name file))
          (loading/notify! "Failed to import STL file — the file may be corrupt or unsupported")))))

(defn- import-step-file! [file text]
  (let [shape (kernel/import-step text (.-name file))]
    (if shape
      (let [name-str (.-name file)
            mesh (kernel/tessellate shape)
            ^js viewer (get-viewer)
            part (sa/build-part name-str mesh)]
        (add-to-scene! name-str shape mesh)
        (when viewer
          (.addPart viewer "/root" part)
          (.presetCamera viewer "iso"))
        (js/console.log (str "imported: " name-str)))
      (do (js/console.warn "import-step: failed to import" (.-name file))
          (loading/notify! "Failed to import STEP file — the file may be corrupt or unsupported")))))

(defn- on-file-selected! [e]
  (when-let [file (first (.-files (.-target e)))]
    (loading/show-loading!)
    (let [name (.-name file)
          lower-name (.toLowerCase name)
          hide #(do (loading/hide-loading!) %)]
      (cond
        (.endsWith lower-name ".stl")
        (-> (read-file-as-arraybuffer! file)
            (.then #(import-stl-file! file %))
            (.then hide)
            (.catch #(do (js/console.warn "import: error reading file" %) (loading/notify! "Failed to read file") (loading/hide-loading!))))
        (or (.endsWith lower-name ".step")
            (.endsWith lower-name ".stp"))
        (-> (read-file-as-text! file)
            (.then #(import-step-file! file %))
            (.then hide)
            (.catch #(do (js/console.warn "import: error reading file" %) (loading/notify! "Failed to read file") (loading/hide-loading!))))
        :else
        (do (js/console.warn "import: unsupported file type:" name)
            (loading/notify! (str "Unsupported file type"))
            (loading/hide-loading!)))))
  ;; reset so re-selecting the same file triggers again
  (set! (.-value (.-target e)) ""))

(defn- upload-svg []
  (let [svg (.createElementNS js/document "http://www.w3.org/2000/svg" "svg")]
    (.setAttribute svg "viewBox" "0 0 24 24")
    (.setAttribute svg "width" "16")
    (.setAttribute svg "height" "16")
    (set! (.. svg -style -display) "block")
    (set! (.. svg -style -fill) "var(--tcv-font-color)")
    (set! (.-innerHTML svg)
      (str
        "<g transform=\"translate(0, 19) scale(1, -1)\">"
        "<path d=\"M12 3c.55 0 1 .45 1 1v8.59l3.29-3.3a1 1 0 0 1 1.42 1.42l-5 5a1 1 0 0 1-1.42 0l-5-5a1 1 0 0 1 1.42-1.42L11 12.59V4c0-.55.45-1 1-1z\"/>"
        "</g>"
        "<path d=\"M4 19c0-.55.45-1 1-1h14c.55 0 1 .45 1 1s-.45 1-1 1H5c-.55 0-1-.45-1-1z\"/>"))
    svg))

(defn- make-file-input []
  (let [input (js/document.createElement "input")]
    (set! (.-type input) "file")
    (set! (.-accept input) ".stl,.step,.stp")
    (set! (.. input -style -display) "none")
    (.addEventListener input "change" on-file-selected!)
    input))

(defn mount-import-button!
  "Mount the STL/STEP file import button into the three-cad-viewer toolbar." [display]
  (let [toolbar (.. display -cadTool -container)]
    (when-not (nil? toolbar)
      (let [file-input (make-file-input)
            help-btn (.querySelector toolbar ".tcv_button_help")
            ref-node (when help-btn (.closest help-btn ".tcv_tooltip"))
            container (js/document.createElement "div")
            tooltip-wrap (js/document.createElement "span")
            button (js/document.createElement "button")]
        (set! (.. container -style -position) "relative")
        (set! (.. container -style -display) "inline-block")
        (set! (.-className tooltip-wrap) "tcv_tooltip")
        (.setAttribute tooltip-wrap "data-tooltip" "Import")
        (.setAttribute tooltip-wrap "data-base-tooltip" "Import")
        (set! (.-className button) "tcv_btn tcv_btn_highlight tcv_round")
        (set! (.. button -style -padding) "0 6px")
        (set! (.. button -style -height) "28px")
        (set! (.. button -style -cursor) "pointer")
        (set! (.. button -style -display) "flex")
        (set! (.. button -style -alignItems) "center")
        (set! (.. button -style -justifyContent) "center")
        (.appendChild button (upload-svg))
        (.appendChild tooltip-wrap button)
        (.appendChild container tooltip-wrap)
        (.appendChild container file-input)
        (if ref-node
          (.insertBefore toolbar container ref-node)
          (.appendChild toolbar container))
        (.addEventListener button "click"
          (fn [_] (.click file-input)))
        (reset! *file-input file-input)))))
