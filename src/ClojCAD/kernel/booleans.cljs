(ns ClojCAD.kernel.booleans
  (:require [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.lifecycle :as lifecycle]))

(defn- oc []
  @init/oc-instance)

(defn- -fuse-2 [shape-a shape-b]
  (try
    (let [ocjs (aget (oc) "OCJS")
          result (.BooleanFuse ocjs shape-a shape-b 1e-7)]
      (when (and result (not (.IsNull result)))
        (lifecycle/track result)
        result))
    (catch :default e
      (js/console.warn "BooleanFuse failed:" e)
      nil)))

(defn- -common-2 [shape-a shape-b]
  (try
    (let [ocjs (aget (oc) "OCJS")
          result (.BooleanCommon ocjs shape-a shape-b 1e-7)]
      (when (and result (not (.IsNull result)))
        (lifecycle/track result)
        result))
    (catch :default e
      (js/console.warn "BooleanCommon failed:" e)
      nil)))

(defn- -cut-2 [shape-a shape-b]
  (try
    (let [ocjs (aget (oc) "OCJS")
          result (.BooleanCut ocjs shape-a shape-b 1e-7)]
      (when (and result (not (.IsNull result)))
        (lifecycle/track result)
        result))
    (catch :default e
      (js/console.warn "BooleanCut failed:" e)
      nil)))

(defn fuse [a b & more]
  (let [result (-fuse-2 a b)]
    (if (nil? result)
      nil
      (reduce (fn [acc s] (when acc (-fuse-2 acc s))) result more))))

(defn common [a b & more]
  (let [result (-common-2 a b)]
    (if (nil? result)
      nil
      (reduce (fn [acc s] (when acc (-common-2 acc s))) result more))))

(defn cut [a b & more]
  (let [result (-cut-2 a b)]
    (if (nil? result)
      nil
      (reduce (fn [acc s] (when acc (-cut-2 acc s))) result more))))
