(ns aero-world.service)

;; Utils

(defn get-random-from-set [set]
  (set (+ 1 (rand-int (count set)))))

;; Aircraft 

(defn rent-aircraft-query [{:keys [aircraft user]}]
  [[:db/add (-> user :db/id) :user/aircraft (-> aircraft :db/id)]
   [:db/add (-> aircraft :db/id) :aircraft/status :aircraft.status/rented]])

(defn leave-aircraft-query [{:keys [aircraft user]}]
  [[:db/retract (-> user :db/id) :user/aircraft (-> aircraft :db/id)]
   [:db/add (-> aircraft :db/id) :aircraft/status :aircraft.status/available]])

;; Flight

(defn create-flight-query [{:keys [from to aircraft user]}]
  [{:flight/created-at (java.util.Date.)
    :flight/from {:airport/icao from}
    :flight/to {:airport/icao to}
    :flight/aircraft aircraft
    :flight/user user
    :flight/status :flight.status/scheduled}])

;; Order

(defn assign-order-query [{:keys [aircraft order]}]
  [[:db/add (-> aircraft :db/id) :aircraft/payload (-> order :db/id)]
   [:db/add (-> order :db/id) :order/status :order.status/assigned]])

(defn remove-order-query [{:keys [order aircraft]}]
  [[:db/retract (-> aircraft :db/id) :aircraft/payload (-> order :db/id)]])

(defn update-user-balance-query [{:keys [user orders]}]
  (let
   [total-value (reduce + (map #(-> % :order/value) orders))
    new-balance (-> user :user/balance)]
    [[:db/add (-> user :db/id) :user/balance (+ total-value new-balance)]]))

(defn create-order-query [{:keys [airport-from airport-to quantity product]}]
  [{:order/from (-> airport-from :db/id)
    :order/to (-> airport-to :db/id)
    :order/product (-> product :db/id)
    :order/quantity quantity
    :order/value (* quantity (-> product :product/value))
    :order/status :order.status/available}])

(defn create-order-random-query [{:keys [airport product]}]
  (let [airport-to (get-random-from-set (-> airport :available-for-orders))
        quantity (+ (rand-int 10))]
    (create-order-query {:airport-from airport
                         :airport-to airport-to
                         :quantity quantity
                         :product product})))

(defn deliver-order-query [{:keys [aircraft orders user]}]
  (vec
   (concat
    (update-user-balance-query {:user user :orders orders})
    (vec (mapcat (fn [order]
                   [[:db/add (-> order :db/id) :order/status :order.status/finished]
                    (first (remove-order-query {:order order :aircraft aircraft}))])
                 orders)))))

;; Finish Flight (Order + Flight)

(defn finish-flight-order-query [{:keys [flight order]}]
  [[:db/add (-> order :db/id) :order/finish-flight (-> flight :db/id)]])

(defn finish-flight-query [{:keys [flight user airport aircraft]}]
  (let [orders (filter #(= (-> % :order/to) airport) (-> aircraft :aircraft/payload))
        flight-orders (vec (mapcat #(finish-flight-order-query {:order % :flight flight}) orders))
        orders-query (deliver-order-query {:aircraft aircraft :orders orders :user user})]
    (vec (concat (if orders-query (vec orders-query) nil)
                 flight-orders
                 [[:db/add (-> user :db/id) :user/airport (-> airport :db/id)]
                  [:db/add (-> aircraft :db/id) :aircraft/airport (-> airport :db/id)]
                  [:db/add (-> flight :db/id) :flight/status :flight.status/finished]]))))