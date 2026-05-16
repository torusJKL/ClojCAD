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

(defn set-default-shape-color! [color]
  (let [c (parse-color color)]
    (reset! *default-shape-color c)
    (js/console.log (str "default shape color set to 0x" (.. c (toString 16))))))

(defn get-default-shape-color []
  @*default-shape-color)

(set! (.-setShapeColor js/window)
  (fn [color]
    (set-default-shape-color! color)))

(defn load-config! []
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
