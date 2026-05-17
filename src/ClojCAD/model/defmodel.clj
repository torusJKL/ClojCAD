(ns ClojCAD.model.defmodel)

(defn- extract-keys [params]
  (if (map? (first params))
    (vec (map keyword (get (first params) :keys)))
    (vec (map keyword params))))

(defmacro defmodel
  "Define a parametric model. Syntax:
     (defmodel name [param1 param2 ...] body)
   The model function is automatically wrapped with reactive-model for caching
   and registered in the global model registry. Supports :opacity metadata on the name." [name params & body]
  (let [opts (or (select-keys (meta &form) [:opacity]) {})
        simple? (not (map? (first params)))
        expanded-params (if simple?
                          [(hash-map :keys (vec params))]
                          params)
        param-keys (extract-keys params)]
    (list 'def name
      (list 'ClojCAD.model.core/reactive-model (list 'quote name) param-keys
        (cons 'fn (cons expanded-params body))
        opts))))
