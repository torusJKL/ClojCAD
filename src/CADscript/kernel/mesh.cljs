(ns CADscript.kernel.mesh
  (:require [CADscript.kernel.init :as init]))

(defn tessellate
  ([shape] (tessellate shape 0.1))
  ([shape deflection]
   (let [oc @init/oc-instance
         mesh (oc.BRepMesh_IncrementalMesh. shape deflection)
         _ (.Perform mesh)
         loc (oc.TopLoc_Location.)
         explorer (oc.TopExp_Explorer. shape (.-TopAbs_FACE oc))
         vertices (atom [])
         indices (atom [])]
     (while (.More explorer)
       (let [face (.Current explorer)
             triangulation (oc.BRep_Tool.Triangulation. face loc)]
         (when (some? triangulation)
           (let [n-nodes (.NbNodes triangulation)
                 n-tris (.NbTriangles triangulation)]
             (doseq [i (range 1 (inc n-nodes))]
               (let [pnt (.Node triangulation i)]
                 (swap! vertices conj (.X pnt))
                 (swap! vertices conj (.Y pnt))
                 (swap! vertices conj (.Z pnt))))
             (doseq [i (range 1 (inc n-tris))]
               (let [tri (.Triangle triangulation i)
                     n1 (.Get1 tri)
                     n2 (.Get2 tri)
                     n3 (.Get3 tri)]
                 (swap! indices conj (dec n1))
                 (swap! indices conj (dec n2))
                 (swap! indices conj (dec n3))))))
         (.Next explorer)))
     {:vertices @vertices
      :indices @indices})))
