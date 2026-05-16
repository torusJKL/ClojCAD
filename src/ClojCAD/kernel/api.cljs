(ns ClojCAD.kernel.api
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.primitives :as primitives]
            [ClojCAD.kernel.booleans :as booleans]
            [ClojCAD.kernel.mesh :as mesh]
            [ClojCAD.kernel.export :as export]
            [ClojCAD.kernel.import :as import]
            [ClojCAD.kernel.lifecycle :as lifecycle]
            [ClojCAD.kernel.text3d :as text3d]
            [ClojCAD.kernel.font :as font]))

(def init-kernel init/init-kernel)
(def oc-instance init/oc-instance)
(def loading? init/loading?)
(def error init/error)

(def make-sphere primitives/make-sphere)
(def make-box primitives/make-box)
(def make-cylinder primitives/make-cylinder)
(def make-cone primitives/make-cone)
(def make-circle primitives/make-circle)
(def make-polygon primitives/make-polygon)

(def translate primitives/translate)
(def rotate primitives/rotate)
(def extrude primitives/extrude)

(def text3d text3d/text3d)
(def register-font! font/register-font!)
(def load-font! font/load-font!)
(def list-fonts font/list-fonts)
(def font-info font/font-info)

(def fuse booleans/fuse)
(def common booleans/common)
(def cut booleans/cut)

(def tessellate mesh/tessellate)

(def export-stl export/export-stl)
(def export-step export/export-step)

(def import-stl import/import-stl)
(def import-step import/import-step)

(def track lifecycle/track)
(def destroy lifecycle/destroy)
(def destroy-all lifecycle/destroy-all)
