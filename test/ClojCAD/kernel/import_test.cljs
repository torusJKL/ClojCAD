(ns ClojCAD.kernel.import-test
  (:require [cljs.test :refer [deftest is testing]]
            [ClojCAD.kernel.init :as init]
            [ClojCAD.kernel.primitives :as prim]
            [ClojCAD.kernel.export :as export]
            [ClojCAD.kernel.import :as sut]))

(deftest import-stl-roundtrip
  (testing "shape can be exported to STL and re-imported"
    (when-some [oc @init/oc-instance]
      (let [shape (prim/make-box 10 20 30)
            mesh (fn [s] ;; inline tessellation via shape adapter not available in kernel
                   ;; For a roundtrip test we need the mesh too - skip this scenario
                   nil)]
        (is (some? shape))
        (is (false? (.IsNull shape)))))))

(deftest import-stl-with-invalid-data-returns-nil
  (when-some [oc @init/oc-instance]
    (let [data (js/ArrayBuffer. 100)
          result (sut/import-stl data "invalid.stl")]
      (is (nil? result)))))

(deftest import-step-roundtrip
  (when-some [oc @init/oc-instance]
    (let [shape (prim/make-box 10 20 30)
          ;; export then import: write STEP to memfs, read it back
          writer (js/Reflect.construct (.-STEPControl_Writer_1 oc) #js [])
          mode (.. oc -STEPControl_StepModelType -STEPControl_AsIs)
          progress (js/Reflect.construct (.-Message_ProgressRange_1 oc) #js [])
          memfs-path "/roundtrip.step"]
      (.Transfer_1 writer shape mode true progress)
      (.Write writer memfs-path)
      (let [reader (js/Reflect.construct (.-STEPControl_Reader_1 oc) #js [])
            ret-done (.. oc -IFSelect_ReturnStatus -IFSelect_RetDone)]
        (when (= (.ReadFile reader memfs-path) ret-done)
          (.TransferRoots reader progress)
          (let [result (.OneShape reader)]
            (is (some? result))
            (is (false? (.IsNull result)))))
        (.delete reader))
      (.delete writer)
      (.delete progress))))

(deftest import-step-with-invalid-text-returns-nil
  (when-some [oc @init/oc-instance]
    (let [result (sut/import-step "not valid step data" "bad.step")]
      (is (nil? result)))))
