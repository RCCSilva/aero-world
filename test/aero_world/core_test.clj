(ns aero-world.core-test
  (:require [clojure.test :refer :all]
            [aero-world.core :refer :all]
            [aero-world.service :refer :all]
            [datomic.api :as d]))

(deftest flight-queries
  (testing "Finish Flight - "
    (is (=
         [[:db/add 2 :order/finish-flight 1]]
         (finish-flight-order-query {:flight {:db/id 1} :order {:db/id 2}})))

    (testing "Finish Flight - Full - Query Without Payload"
      (is (=
           [[:db/add 1 :user/balance 10]
            [:db/add 1 :user/airport 2]
            [:db/add 4 :aircraft/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]]
           (finish-flight-query {:user  {:db/id 1 :user/balance 10}
                                 :airport {:db/id 2}
                                 :flight {:db/id 3}
                                 :aircraft {:db/id 4 :aircraft/payload #{}}}))))
    
    (testing "Finish Flight - Full - Query With Payload of Another Airport"
      (is (=
           [[:db/add 1 :user/balance 10]
            [:db/add 1 :user/airport 2]
            [:db/add 4 :aircraft/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]]
           (finish-flight-query {:user  {:db/id 1 :user/balance 10}
                                 :airport {:db/id 2}
                                 :flight {:db/id 3}
                                 :aircraft {:db/id 4 :aircraft/payload #{{:db/id 5
                                                                          :order/to {:db/id 99}
                                                                          :order/value 10}}}}))))

    (testing "Finish Flight - Full - Query With Payload"
      (is (=
           [[:db/add 1 :user/balance 20]
            [:db/add 5 :order/status :order.status/finished]
            [:db/retract 4 :aircraft/payload 5]
            [:db/add 5 :order/finish-flight 3]
            [:db/add 1 :user/airport 2]
            [:db/add 4 :aircraft/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]]
           (finish-flight-query {:user  {:db/id 1 :user/balance 10}
                                 :airport {:db/id 2}
                                 :flight {:db/id 3}
                                 :aircraft {:db/id 4 :aircraft/payload #{{:db/id 5
                                                                          :order/to {:db/id 2}
                                                                          :order/value 10}}}})))))
  
  (testing "Create Flight"
    (is (=
         [{:flight/created-at (java.util.Date.)
           :flight/from {:airport/icao "SBGR"}
           :flight/to {:airport/icao "SBPA"}
           :flight/aircraft 123456789
           :flight/user 987654321
           :flight/status :flight.status/scheduled}]
         (create-flight-query {:from "SBGR" :to "SBPA" :aircraft 123456789 :user 987654321})))))

(deftest aircraft-queries
  (testing "Rent aircraft"
    (is (= [[:db/add 1 :user/aircraft 2]
            [:db/add 2 :aircraft/status :aircraft.status/rented]]
           (rent-aircraft-query {:user {:db/id 1} :aircraft {:db/id 2}}))))

  (testing "Leave aircraft"
    (is (= [[:db/retract 1 :user/aircraft 2]
            [:db/add 2 :aircraft/status :aircraft.status/available]]
           (leave-aircraft-query {:user {:db/id 1} :aircraft {:db/id 2}})))))

(deftest order-queries
  (testing "Asign Order"
    (is (= [[:db/add 1 :aircraft/payload 2]
            [:db/add 2 :order/status :order.status/assigned]]
           (assign-order-query {:aircraft {:db/id 1} :order {:db/id 2}}))))
  
  (testing "Remove Order from Aircraft"
    (is (= [[:db/retract 1 :aircraft/payload 2]]
           (remove-order-query {:aircraft {:db/id 1} :order {:db/id 2}}))))

  (testing "Balance Query"
    (is (= [[:db/add 1 :user/balance 10]]
           (update-user-balance-query {:user {:db/id 1 :user/balance 0}
                                       :orders '({:order/value 10})}))))

  (testing "Deliver Order"
    (is (= [[:db/add 3 :user/balance 4]
            [:db/add 2 :order/status :order.status/finished]
            [:db/retract 1 :aircraft/payload 2]]
           (deliver-order-query {:aircraft {:db/id 1}
                                 :orders '({:db/id 2 :order/value 3})
                                 :user {:db/id 3 :user/balance 1}}))))
  (testing "Define orders"
    (is (= [[:db/add 1 :user/balance 40]]
           (update-user-balance-query
            {:user {:db/id 1 :user/balance 0}
             :orders '({:order/value 10} {:order/value 30})}))))
  
  (testing "Create Offer"
    (is (=
         [{:order/from 1
           :order/to 2
           :order/product 3
           :order/quantity 10
           :order/value 100
           :order/status :order.status/available}]
         (create-order-query {:airport-from {:db/id 1}
                              :airport-to {:db/id 2}
                              :product {:db/id 3 :product/value 10}
                              :quantity 10}))))
  
  (testing "Create Offer - With Random"
    (let [airport {:db/id 1
                   :airport/available-for-orders #{{:db/id 2}}}
          product {:db/id 3 :product/value 10}
          data (first (create-order-random-query {:airport airport
                                                  :product product}))
          quantity (data :order/quantity)] 
      (is (and
           (= (data :order/from) 1)
           (= (data :order/to) 2)
           (= (data :order/product) 3)
           (contains? (set (range 1 11)) quantity)
           (= (data :order/value) (* quantity (product :product/value)))
           (= (data :order/status) :order.status/available))))))
