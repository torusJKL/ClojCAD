(ns CADscript.model.defmodel)

(defn- extract-keys [params]
  (if (map? (first params))
    (vec (map keyword (get (first params) :keys)))
    (vec (map keyword params))))

(defmacro defmodel [name params & body]
  (let [opts (or (meta &form) {})
        simple? (not (map? (first params)))
        expanded-params (if simple?
                          [(hash-map :keys (vec params))]
                          params)
        param-keys (extract-keys params)]
    (list 'def name
      (list 'CADscript.model.core/reactive-model (list 'quote name) param-keys
        (cons 'fn (cons expanded-params body))
        opts))))
