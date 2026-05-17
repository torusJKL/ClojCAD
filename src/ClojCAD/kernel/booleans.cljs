(ns ClojCAD.kernel.booleans
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.lifecycle :as lifecycle]))

(defn- ^js oc []
  @init/oc-instance)

(defn- -fuse-2 [^js shape-a ^js shape-b]
  (try
    (let [^js ocjs (aget (oc) "OCJS")
          ^js result (.BooleanFuse ocjs shape-a shape-b 1e-7)]
      (when (and result (not (.IsNull result)))
        (lifecycle/track result)
        result))
    (catch :default e
      (js/console.warn "BooleanFuse failed:" e)
      nil)))

(defn- -common-2 [^js shape-a ^js shape-b]
  (try
    (let [^js ocjs (aget (oc) "OCJS")
          ^js result (.BooleanCommon ocjs shape-a shape-b 1e-7)]
      (when (and result (not (.IsNull result)))
        (lifecycle/track result)
        result))
    (catch :default e
      (js/console.warn "BooleanCommon failed:" e)
      nil)))

(defn- -cut-2 [^js shape-a ^js shape-b]
  (try
    (let [^js ocjs (aget (oc) "OCJS")
          ^js result (.BooleanCut ocjs shape-a shape-b 1e-7)]
      (when (and result (not (.IsNull result)))
        (lifecycle/track result)
        result))
    (catch :default e
      (js/console.warn "BooleanCut failed:" e)
      nil)))

(defn fuse
  "Boolean union (addition) of two or more shapes. Returns a new shape representing
   the combination of all input shapes. Returns nil if the operation fails." [a b & more]
  (let [result (-fuse-2 a b)]
    (if (nil? result)
      nil
      (reduce (fn [acc s] (when acc (-fuse-2 acc s))) result more))))

(defn common
  "Boolean intersection of two or more shapes. Returns the volume common to all input
   shapes. Returns nil if the shapes do not intersect or the operation fails." [a b & more]
  (let [result (-common-2 a b)]
    (if (nil? result)
      nil
      (reduce (fn [acc s] (when acc (-common-2 acc s))) result more))))

(defn cut
  "Boolean subtraction of shape b (and subsequent shapes) from shape a.
   Returns a new shape with the subtracted volumes removed. Returns nil if the operation fails." [a b & more]
  (let [result (-cut-2 a b)]
    (if (nil? result)
      nil
      (reduce (fn [acc s] (when acc (-cut-2 acc s))) result more))))
