(ns CADscript.kernel.api
  (:require [CADscript.kernel.init :as init]
            [CADscript.kernel.primitives :as primitives]
            [CADscript.kernel.mesh :as mesh]
            [CADscript.kernel.lifecycle :as lifecycle]))

(def init-kernel init/init-kernel)
(def oc-instance init/oc-instance)
(def loading? init/loading?)
(def error init/error)

(def make-sphere primitives/make-sphere)
(def make-box primitives/make-box)
(def make-cylinder primitives/make-cylinder)
(def make-cone primitives/make-cone)

(def tessellate mesh/tessellate)

(def track lifecycle/track)
(def destroy lifecycle/destroy)
(def destroy-all lifecycle/destroy-all)
