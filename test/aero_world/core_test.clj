(ns aero-world.core-test
  (:use aero-world.db.core)
  (:require [clojure.test :refer :all]
            [aero-world.core :refer :all]
            [datomic.api :as d]))

(deftest create-flight-test
  (with-redefs [conn (create-empty-in-memory-db)]
    (testing "Creating an Aiport"
      (is (= #{["SBGR"]}
             (do
               (add-airport "SBGR")
               (find-all-airports)))))
    
    (testing "Creating a Aircraft"
      (is (= #{["C172"]}
             (do
               (add-aircraft "C172")
               (find-all-aircrafts)))))
    
    (testing "Creating a Flight"
      (is (= #{[]}
             (do
               (add-flight "SBGR" "SBGR" "C172")
               (find-all-flights)))))))
