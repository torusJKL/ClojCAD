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

(def init-kernel
  "Initialize the OpenCASCADE WASM kernel and bundled fonts."
  init/init-kernel)
(def oc-instance
  "Atom containing the OpenCASCADE.js module instance after initialization."
  init/oc-instance)
(def loading?
  "Atom indicating whether the kernel is still loading."
  init/loading?)
(def error
  "Atom containing any kernel initialization error message."
  init/error)

(def make-sphere
  "Create a solid sphere with the given radius."
  primitives/make-sphere)
(def make-box
  "Create a rectangular box with dimensions dx, dy, dz. Optional centered? parameter."
  primitives/make-box)
(def make-cylinder
  "Create a cylinder with the given radius and height. Optional centered? parameter."
  primitives/make-cylinder)
(def make-cone
  "Create a truncated cone with bottom radius1, top radius2, and height."
  primitives/make-cone)
(def make-circle
  "Create a circle of the given radius. Returns a face or wire."
  primitives/make-circle)
(def make-polygon
  "Create a polygon from a sequence of [x y z] points. Returns a face or wire."
  primitives/make-polygon)

(def translate
  "Translate (move) a shape by the given x, y, z offsets."
  primitives/translate)
(def rotate
  "Rotate a shape around the given axis by the specified degrees."
  primitives/rotate)
(def extrude
  "Extrude a planar face along a direction vector [dx dy dz]."
  primitives/extrude)

(def text3d
  "Create a 3D text shape from a string. Options: :font, :height."
  text3d/text3d)
(def register-font!
  "Register a custom font from a URL."
  font/register-font!)
(def load-font!
  "Synchronously load and register a font from a URL."
  font/load-font!)
(def list-fonts
  "Return a list of all registered font names."
  font/list-fonts)
(def font-info
  "Return metadata for a registered font."
  font/font-info)

(def fuse
  "Boolean union (addition) of two or more shapes."
  booleans/fuse)
(def common
  "Boolean intersection of two or more shapes."
  booleans/common)
(def cut
  "Boolean subtraction of one or more shapes from a base shape."
  booleans/cut)

(def tessellate
  "Tessellate a shape into a mesh for rendering or export."
  mesh/tessellate)

(def export-stl
  "Export one or more shapes to a binary STL file."
  export/export-stl)
(def export-step
  "Export one or more shapes to a STEP file."
  export/export-step)

(def import-stl
  "Import an STL file from an ArrayBuffer. Returns a TopoDS_Shape or nil."
  import/import-stl)
(def import-step
  "Import a STEP file from a text string. Returns a TopoDS_Shape or nil."
  import/import-step)

(def track
  "Register an OpenCASCADE object for lifecycle management."
  lifecycle/track)
(def destroy
  "Delete an OpenCASCADE object."
  lifecycle/destroy)
(def destroy-all
  "Delete all tracked OpenCASCADE objects."
  lifecycle/destroy-all)
