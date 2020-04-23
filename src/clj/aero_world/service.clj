(ns aero-world.service
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]))

;; Utils

(defn get-random-from-set [x]
  ((vec (map (fn [x] x) x)) (rand-int (count x))))

(defn get-random [x]
  (x (rand-int (count x))))

(defn base-query? [x]
  (keyword? (first x)))

(defn flatten-query [x]
  "Receives a list (maybe list of list) and flatten it with base queries, returning a list of base queries"
  (cond
    (= (count x) 0) nil
    (base-query? (first x)) (cons (first x) (flatten-query (rest x)))
    (not (base-query? (first x))) (cons (flatten-query (first x)) (flatten-query (rest x)))))

;; User 

(defn update-user-balance-query [{:keys [user value]}]
  [[:db/add (-> user :db/id) :user/balance (+ (-> user :user/balance) value)]])

(defn update-user-airport-query [{:keys [user airport]}]
  [[:db/add (-> user :db/id) :user/airport (-> airport :db/id)]])

;; Aircraft 

(defn update-aircraft-airport-query  [{:keys [aircraft airport]}]
  [[:db/add (-> aircraft :db/id) :aircraft/airport (-> airport :db/id)]])

(defn get-aircraft-rent-value [{:keys [flight closed-at]}]
  (let [seconds-elapsed (t/in-seconds (t/interval (-> flight :flight/created-at c/from-date)
                                                  (c/from-date closed-at)))
        user-owns-the-aircraft? (= (-> flight :flight/aircraft :aircraft/owner :db/id)
                                   (-> flight :flight/user :db/id))]
    (if user-owns-the-aircraft? ;; If the user owns the aircraft, she does not need to pay for it
      0
      (* -1
         seconds-elapsed
         (/ (-> flight :flight/aircraft :aircraft/rent-price-per-hour)
            3600)))))

(defn make-aircraft-available-query [aircraft]
  [[:db/add (-> aircraft :db/id) :aircraft/status :aircraft.status/available]])

(defn leave-aircraft-query [{:keys [aircraft user]}]
  (concat
   (make-aircraft-available-query aircraft)
   [[:db/retract (-> user :db/id) :user/aircraft (-> aircraft :db/id)]]))

(defn rent-aircraft-query [{:keys [aircraft user]}]
  (let [aircraft-available (if (-> user :user/aircraft)
                             (make-aircraft-available-query (-> user :user/aircraft))
                             nil)]
   (concat aircraft-available
           [[:db/add (-> user :db/id) :user/aircraft (-> aircraft :db/id)]
            [:db/add (-> aircraft :db/id) :aircraft/status :aircraft.status/rented]])))

(defn make-aircraft-available-for-rent-query [aircraft]
  [[:db/add (-> aircraft :db/id) :aircraft/available-for-rent?  true]])

(defn make-aircraft-unavailable-for-rent-query [aircraft]
  [[:db/add (-> aircraft :db/id) :aircraft/available-for-rent?  false]])

(defn pay-rent-aircraft-query [{:keys [flight closed-at]}]
  (update-user-balance-query {:user (-> flight :flight/user)
                              :value (get-aircraft-rent-value {:flight flight
                                                               :closed-at closed-at})}))

;; Flight

(defn create-flight-query [{:keys [from to aircraft user]}]
  [{:flight/created-at (java.util.Date.)
    :flight/from {:airport/icao from}
    :flight/to {:airport/icao to}
    :flight/aircraft aircraft
    :flight/user user
    :flight/status :flight.status/scheduled}])

;; Order

(defn get-order-value [order]
  (-> order :order/value))

(defn set-order-status-to-finished-query [order]
  [[:db/add (-> order :db/id) :order/status :order.status/finished]])

(defn assign-order-query [{:keys [aircraft order]}]
  [[:db/add (-> aircraft :db/id) :aircraft/payload (-> order :db/id)]
   [:db/add (-> order :db/id) :order/status :order.status/assigned]])

(defn remove-order-from-aircraft-payload-query [{:keys [order aircraft]}]
  [[:db/retract (-> aircraft :db/id) :aircraft/payload (-> order :db/id)]])

(defn create-order-query [{:keys [airport-from airport-to quantity product]}]
  [{:order/from (-> airport-from :db/id)
    :order/to (-> airport-to :db/id)
    :order/product (-> product :db/id)
    :order/quantity quantity
    :order/value (* quantity (-> product :product/value))
    :order/status :order.status/available}])

(defn create-order-random-query [{:keys [airport product]}]
  (let [airport-to (get-random-from-set (-> airport :airport/available-orders))
        quantity (+ (rand-int 10) 1)]
    (create-order-query {:airport-from airport
                         :airport-to airport-to
                         :quantity quantity
                         :product product})))

(defn deliver-one-order-no-user-balance-query [{:keys [aircraft order]}]
  (concat
   (set-order-status-to-finished-query order)
   (remove-order-from-aircraft-payload-query {:order order
                                              :aircraft aircraft})))

(defn deliver-one-order-query [{:keys [aircraft order user]}]
  (concat (update-user-balance-query {:user user
                                      :value (get-order-value order)})
          (deliver-one-order-no-user-balance-query {:aircraft aircraft
                                                    :order order})))



;; Finish Flight (Order + Flight)

(defn finish-flight-order-query [{:keys [flight order]}]
  [[:db/add (-> order :db/id) :order/finish-flight (-> flight :db/id)]])

(defn set-flight-status-to-finished-query [flight]
  [[:db/add (-> flight :db/id) :flight/status :flight.status/finished]])

(defn update-flight-closed-at [{:keys [flight closed-at]}]
  [[:db/add (-> flight :db/id) :flight/closed-at closed-at]])

(defn get-orders-for-one-airport [{:keys [orders airport]}]
  (filter #(= (-> % :order/to) airport) orders))

(defn finish-flight-query [{:keys [flight closed-at]}]
  (let [airport (-> flight :flight/to)
        aircraft (-> flight :flight/aircraft)
        user (-> flight :flight/user)
        ;; Gets all orders from the arrival airport
        orders (get-orders-for-one-airport {:airport airport
                                            :orders (-> aircraft :aircraft/payload)})
        ;; Sets as the order's finish-flight the current flight 
        flight-orders (mapcat #(finish-flight-order-query {:order % :flight flight}) orders)
        ;; Set orders as finished and remove them from aircraft's payload
        orders-query (mapcat
                      #(deliver-one-order-no-user-balance-query {:aircraft aircraft
                                                                 :order %})
                      orders)
        value (float (+ (reduce + (map get-order-value orders))
                         (get-aircraft-rent-value {:flight flight
                                                   :closed-at closed-at})))]
    (vec
     (concat
      (update-user-balance-query {:user user :value value}) 
      orders-query
      flight-orders
      (update-user-airport-query {:user user :airport airport})
      (set-flight-status-to-finished-query flight)
      (update-flight-closed-at {:flight flight :closed-at closed-at})
      (update-aircraft-airport-query {:aircraft aircraft :airport airport})))))