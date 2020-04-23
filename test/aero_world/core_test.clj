(ns aero-world.core-test
  (:require [clojure.test :refer :all]
            [aero-world.core :refer :all]
            [aero-world.service :refer :all]
            [datomic.api :as d]
            [clojure.instant :refer [read-instant-date]]))

(deftest flight-queries
  (testing "Finish Flight - "
    (is (=
         [[:db/add 2 :order/finish-flight 1]]
         (finish-flight-order-query {:flight {:db/id 1} :order {:db/id 2}})))

    (testing "Finish Flight - Full - Query Without Payload"
      (is (=
           [[:db/add 1 :user/balance 100.0]
            [:db/add 1 :user/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]
            [:db/add 3 :flight/closed-at #inst "2020-01-01T03:00:00.000-00:00"]
            [:db/add 4 :aircraft/airport 2]]
           (finish-flight-query {:flight {:db/id 3
                                          :flight/created-at (read-instant-date "2020-01-01T02:00:00")
                                          :flight/to {:db/id 2}
                                          :flight/user {:db/id 1 :user/balance 100}
                                          :flight/aircraft {:db/id 4
                                                            :aircraft/rent-price-per-hour 100.0
                                                            :aircraft/owner {:db/id 1}
                                                            :aircraft/payload #{}}}
                                 :closed-at (read-instant-date "2020-01-01T03:00:00")}))))
    
    (testing "Finish Flight - Full - Query with payload to the arrival airport"
      (is (=
           [[:db/add 1 :user/balance 110.0]
            [:db/add 99 :order/status :order.status/finished]
            [:db/retract 4 :aircraft/payload 99]
            [:db/add 99 :order/finish-flight 3]
            [:db/add 1 :user/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]
            [:db/add 3 :flight/closed-at #inst "2020-01-01T03:00:00.000-00:00"]
            [:db/add 4 :aircraft/airport 2]]
           (finish-flight-query {:flight {:db/id 3
                                          :flight/created-at (read-instant-date "2020-01-01T02:00:00")
                                          :flight/to {:db/id 2}
                                          :flight/user {:db/id 1 :user/balance 100}
                                          :flight/aircraft {:db/id 4
                                                            :aircraft/rent-price-per-hour 100.0
                                                            :aircraft/owner {:db/id 1}
                                                            :aircraft/payload #{{:db/id 99 
                                                                                 :order/to {:db/id 2}
                                                                                 :order/value 10}}}}
                                 :closed-at (read-instant-date "2020-01-01T03:00:00")}))))

    (testing "Finish Flight - Full - Query with payload to arrival airport and aircraft was rented"
      (is (=
           [[:db/add 1 :user/balance 10.0]
            [:db/add 99 :order/status :order.status/finished]
            [:db/retract 4 :aircraft/payload 99]
            [:db/add 99 :order/finish-flight 3]
            [:db/add 1 :user/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]
            [:db/add 3 :flight/closed-at #inst "2020-01-01T03:00:00.000-00:00"]
            [:db/add 4 :aircraft/airport 2]]
           (finish-flight-query {:flight {:db/id 3
                                          :flight/created-at (read-instant-date "2020-01-01T02:00:00")
                                          :flight/to {:db/id 2}
                                          :flight/user {:db/id 1 :user/balance 100}
                                          :flight/aircraft {:db/id 4
                                                            :aircraft/rent-price-per-hour 100.0
                                                            :aircraft/owner {:db/id 2}
                                                            :aircraft/payload #{{:db/id 99
                                                                                 :order/to {:db/id 2}
                                                                                 :order/value 10}}}}
                                 :closed-at (read-instant-date "2020-01-01T03:00:00")}))))
    
    (testing "Finish Flight - Full - Query with payload to another airport the another airport"
      (is (=
           [[:db/add 1 :user/balance 0.0]
            [:db/add 1 :user/airport 2]
            [:db/add 3 :flight/status :flight.status/finished]
            [:db/add 3 :flight/closed-at #inst "2020-01-01T03:00:00.000-00:00"]
            [:db/add 4 :aircraft/airport 2]]
           (finish-flight-query {:flight {:db/id 3
                                          :flight/created-at (read-instant-date "2020-01-01T02:00:00")
                                          :flight/to {:db/id 2}
                                          :flight/user {:db/id 1 :user/balance 100}
                                          :flight/aircraft {:db/id 4
                                                            :aircraft/rent-price-per-hour 100.0
                                                            :aircraft/owner {:db/id 5}
                                                            :aircraft/payload #{{:db/id 99
                                                                                 :order/to {:db/id 3}
                                                                                 :order/value 10}}}}
                                 :closed-at (read-instant-date "2020-01-01T03:00:00")})))))
  
  (testing "Create Flight"
    (is (=
         [{:flight/created-at #inst "2020-01-01T03:00:00.000-00:00"
           :flight/from {:airport/icao "SBGR"}
           :flight/to {:airport/icao "SBPA"}
           :flight/aircraft 123456789
           :flight/user 987654321
           :flight/status :flight.status/scheduled}]
         (create-flight-query {:from "SBGR" 
                               :to "SBPA" 
                               :aircraft 123456789 
                               :user 987654321
                               :created-at #inst "2020-01-01T03:00:00.000-00:00"})))))

(deftest user-queris
  (testing "Balance Query - Add value"
    (is (= [[:db/add 1 :user/balance 100]]
           (update-user-balance-query {:user {:db/id 1 :user/balance 90} 
                                       :value 10}))))

  (testing "Balance Query - Remove value"
    (is (= [[:db/add 1 :user/balance 100]]
           (update-user-balance-query {:user {:db/id 1 :user/balance 110} 
                                       :value -10}))))
  (testing "Airport Query - New Airport"
    (is (= [[:db/add 1 :user/airport 2]]
           (update-user-airport-query {:user {:db/id 1 :user/airport 1}
                                       :airport {:db/id 2}}))))
  )

(deftest aircraft-queries
  (testing "Rent aircraft"
    (is (= [[:db/add 1 :user/aircraft 2]
            [:db/add 2 :aircraft/status :aircraft.status/rented]]
           (rent-aircraft-query {:user {:db/id 1} :aircraft {:db/id 2}}))))

  (testing "Leave aircraft"
    (is (= [[:db/add 2 :aircraft/status :aircraft.status/available]
            [:db/retract 1 :user/aircraft 2]]
           (leave-aircraft-query {:user {:db/id 1} :aircraft {:db/id 2}}))))
  
  (testing "Flight Pay Rent - User does not own the aircraft and it was rented for 1 hour"
    (let [created-at (read-instant-date "2020-01-01T02:00:00")
          closed-at (read-instant-date "2020-01-01T03:00:00")]
      (is (= [[:db/add 1 :user/balance 0]]
             (pay-rent-aircraft-query {:flight {:flight/user {:db/id 1 :user/balance 100}
                                                :flight/created-at created-at
                                                :flight/aircraft {:aircraft/rent-price-per-hour 100
                                                                  :aircraft/owner {:db/id 2}}}
                                       :closed-at closed-at})))))
  (testing "Flight Pay Rent - User owns the aircraft"
    (let [created-at (read-instant-date "2020-01-01T02:00:00")
          closed-at (read-instant-date "2020-01-01T03:00:00")]
      (is (= [[:db/add 1 :user/balance 100]]
             (pay-rent-aircraft-query {:flight {:flight/user {:db/id 1 :user/balance 100}
                                                :flight/created-at created-at
                                                :flight/aircraft {:aircraft/rent-price-per-hour 100
                                                                  :aircraft/owner {:db/id 1}}}
                                       :closed-at closed-at}))))))

(deftest order-queries
  (testing "Asign Order"
    (is (= [[:db/add 1 :aircraft/payload 2]
            [:db/add 2 :order/status :order.status/assigned]]
           (assign-order-query {:aircraft {:db/id 1} :order {:db/id 2}}))))

  (testing "Deliver Order"
    (is (= [[:db/add 3 :user/balance 4]
            [:db/add 2 :order/status :order.status/finished]
            [:db/retract 1 :aircraft/payload 2]]
           (deliver-one-order-query {:aircraft {:db/id 1}
                                     :order {:db/id 2 :order/value 3}
                                     :user {:db/id 3 :user/balance 1}}))))

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
                   :airport/available-orders #{{:db/id 2}}}
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
