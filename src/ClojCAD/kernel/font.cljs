(ns ClojCAD.kernel.font
  (:require ["opentype.js" :as opentype]))

(defonce registry (atom {}))
(defonce bundled-names (atom #{}))

(def ^:private bundled-fonts
  [{:name "Cousine"         :url "/fonts/Cousine-Regular.ttf"}
   {:name "Cousine-Bold"    :url "/fonts/Cousine-Bold.ttf"}
   {:name "Cousine-Italic"  :url "/fonts/Cousine-Italic.ttf"}
   {:name "Cousine-BoldItalic" :url "/fonts/Cousine-BoldItalic.ttf"}])

(defn lookup-font [name]
  (get @registry name))

(defn list-fonts []
  (keys @registry))

(defn font-info [name]
  (when-let [font (lookup-font name)]
    {:name name
     :source (if (@bundled-names name) :bundled :custom)
     :family (.. font -names -fontFamily)
     :style (.. font -names -fontSubfamily)
     :glyphs (.-length (.-glyphs font))}))

(defn- open-db []
  (js/Promise.
    (fn [resolve reject]
      (let [request (.open js/indexedDB "clojcad-fonts" 1)]
        (set! (.-onupgradeneeded request)
          (fn [e]
            (let [db (.-target.result e)]
              (when-not (.contains (.-objectStoreNames db) "fonts")
                (.createObjectStore db "fonts")))))
        (set! (.-onsuccess request)
          (fn [e] (resolve (.-target.result e))))
        (set! (.-onerror request)
          (fn [e] (reject (.-error e))))))))

(defn- put-font! [db name data]
  (js/Promise.
    (fn [resolve reject]
      (let [tx (.transaction db "fonts" "readwrite")
            store (.objectStore tx "fonts")
            request (.put store data name)]
        (set! (.-onsuccess request) (fn [_] (resolve true)))
        (set! (.-onerror request) (fn [e] (reject (.-error e))))))))

(defn- get-all-fonts [db]
  (js/Promise.
    (fn [resolve reject]
      (let [tx (.transaction db "fonts" "readonly")
            store (.objectStore tx "fonts")
            request (.getAll store)]
        (set! (.-onsuccess request)
          (fn [e] (resolve (.-result e))))
        (set! (.-onerror request)
          (fn [e] (reject (.-error e))))))))

(defn- get-all-keys [db]
  (js/Promise.
    (fn [resolve reject]
      (let [tx (.transaction db "fonts" "readonly")
            store (.objectStore tx "fonts")
            request (.getAllKeys store)]
        (set! (.-onsuccess request)
          (fn [e] (resolve (.-result e))))
        (set! (.-onerror request)
          (fn [e] (reject (.-error e))))))))

(defn- restore-from-idb! []
  (-> (open-db)
      (.then (fn [db]
        (close-db-after db
          (-> (js/Promise.all
                #js [(get-all-keys db) (get-all-fonts db)])
              (.then (fn [[keys bufs]]
                (doseq [[k buf] (map vector keys bufs)]
                  (try
                    (let [font (opentype/parse buf)]
                      (swap! registry assoc k font))
                    (catch js/Error e
                      (js/console.warn "Failed to restore font" k e))))))))))
      (.catch (fn [e]
        (js/console.warn "IndexedDB unavailable, fonts not restored:" e)))))

(defn load-bundled-fonts! []
  (-> (js/Promise.all
        (clj->js
          (map (fn [{:keys [name url]}]
                 (-> (js/fetch url)
                     (.then (fn [resp] (.arrayBuffer resp)))
                     (.then (fn [buf]
                       (try
                         (let [font (opentype/parse buf)]
                           (swap! registry assoc name font)
                           (swap! bundled-names conj name))
                         (catch js/Error e
                           (js/console.warn "Failed to parse font" name e)))))))
               bundled-fonts)))
      (.then (fn [_] (restore-from-idb!)))))

(defn- close-db-after [db promise]
  (-> promise
      (.then (fn [v] (.close db) v))
      (.catch (fn [e] (.close db) (throw e)))))

(defn register-font! [name url]
  (-> (js/fetch url)
      (.then (fn [resp] (.arrayBuffer resp)))
      (.then (fn [buf]
        (let [font (opentype/parse buf)]
          (swap! registry assoc name font)
          (-> (open-db)
              (.then (fn [db]
                (close-db-after db (put-font! db name buf))))
              (.catch (fn [e]
                (js/console.warn "IndexedDB unavailable, font not persisted:" e))))
          font)))))

(defn load-font! [name url]
  (try
    (let [req (js/XMLHttpRequest.)
          _ (.open req "GET" url false)
          _ (set! (.-responseType req) "arraybuffer")
          _ (.send req)
          buf (.-response req)]
      (if buf
        (let [font (opentype/parse buf)]
          (swap! registry assoc name font)
          (-> (open-db)
              (.then (fn [db]
                (close-db-after db (put-font! db name buf))))
              (.catch (fn [e]
                (js/console.warn "IndexedDB unavailable, font not persisted:" e))))
          font)
        (do
          (js/console.warn "Failed to load font:" name url (.-status req))
          nil)))
    (catch js/Error e
      (js/console.warn "Failed to load font:" name url e)
      nil)))
