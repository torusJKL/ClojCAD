(ns ClojCAD.viewport.loading)

(defonce *injected-style (atom false))
(defonce *overlay (atom nil))

(defn- inject-style! []
  (when-not @*injected-style
    (let [style (js/document.createElement "style")]
      (set! (.-textContent style)
        "@keyframes clojcad-spin{to{transform:rotate(360deg)}}")
      (js/document.head.appendChild style)
      (reset! *injected-style true))))

(defn show-loading!
  "Show a full-screen loading spinner overlay." []
  (inject-style!)
  (when (nil? @*overlay)
    (let [overlay (js/document.createElement "div")]
      (set! (.. overlay -style -position) "fixed")
      (set! (.. overlay -style -top) "0")
      (set! (.. overlay -style -left) "0")
      (set! (.. overlay -style -width) "100%")
      (set! (.. overlay -style -height) "100%")
      (set! (.. overlay -style -background) "rgba(0,0,0,0.35)")
      (set! (.. overlay -style -zIndex) "9999")
      (set! (.. overlay -style -display) "flex")
      (set! (.. overlay -style -alignItems) "center")
      (set! (.. overlay -style -justifyContent) "center")
      (let [spinner (js/document.createElement "div")]
        (set! (.. spinner -style -width) "36px")
        (set! (.. spinner -style -height) "36px")
        (set! (.. spinner -style -border) "4px solid rgba(255,255,255,0.3)")
        (set! (.. spinner -style -borderTopColor) "#fff")
        (set! (.. spinner -style -borderRadius) "50%")
        (set! (.. spinner -style -animation) "clojcad-spin 0.8s linear infinite")
        (.appendChild overlay spinner))
      (js/document.body.appendChild overlay)
      (reset! *overlay overlay))))

(defn hide-loading!
  "Hide the loading spinner overlay." []
  (when-let [o @*overlay]
    (js/document.body.removeChild o)
    (reset! *overlay nil)))

(defn notify!
  "Show a temporary toast notification with the given message. Auto-hides after 4 seconds." [msg]
  (let [toast (js/document.createElement "div")]
    (set! (.. toast -style -position) "fixed")
    (set! (.. toast -style -bottom) "24px")
    (set! (.. toast -style -left) "50%")
    (set! (.. toast -style -transform) "translateX(-50%)")
    (set! (.. toast -style -background) "rgba(200,50,50,0.9)")
    (set! (.. toast -style -color) "#fff")
    (set! (.. toast -style -padding) "10px 20px")
    (set! (.. toast -style -borderRadius) "6px")
    (set! (.. toast -style -fontSize) "14px")
    (set! (.. toast -style -zIndex) "10000")
    (set! (.. toast -style -maxWidth) "80%")
    (set! (.. toast -style -textAlign) "center")
    (set! (.. toast -style -boxShadow) "0 2px 8px rgba(0,0,0,0.3)")
    (set! (.-textContent toast) msg)
    (js/document.body.appendChild toast)
    (js/setTimeout #(when (.-parentNode toast)
                     (.removeChild (.-parentNode toast) toast))
                  4000)))
