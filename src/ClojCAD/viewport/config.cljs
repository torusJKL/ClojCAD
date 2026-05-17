(ns ClojCAD.viewport.config
  (:require [cljs.reader :as reader]))

(defonce *default-shape-color (atom 0xfbd92c))

(defn- parse-color [color]
  (cond
    (number? color) color
    (string? color) (let [s (if (.startsWith color "#")
                              (subs color 1)
                              color)]
                      (js/parseInt (str "0x" s) 16))
    :else (throw (js/Error. (str "invalid color: " color)))))

(defn set-default-shape-color!
  "Set the default color for newly created shapes. Accepts a hex number or CSS color string." [color]
  (let [c (parse-color color)]
    (reset! *default-shape-color c)
    (js/console.log (str "default shape color set to 0x" (.. c (toString 16))))))

(defn get-default-shape-color
  "Return the current default shape color as a hex number." []
  @*default-shape-color)

(when (and (exists? js/window) (not (undefined? js/window)))
  (set! (.-setShapeColor js/window)
    (fn [color]
      (set-default-shape-color! color))))

(defn load-config!
  "Load configuration from config.edn. Currently reads :default-shape-color.
   Returns a Promise." []
  (-> (js/fetch "config.edn")
      (.then (fn [resp]
               (if (.-ok resp)
                 (.text resp)
                 (js/Promise.reject (str "config.edn not found: " (.-status resp))))))
      (.then (fn [text]
               (try
                 (let [cfg (reader/read-string text)]
                   (when-let [color (:default-shape-color cfg)]
                     (reset! *default-shape-color (parse-color color))
                     (js/console.log (str "loaded default shape color from config.edn: 0x" (.. color (toString 16))))))
                 (catch :default e
                   (js/console.warn "failed to parse config.edn:" e)))))
      (.catch (fn [_]
                (js/console.log "no config.edn found, using default color")))))
