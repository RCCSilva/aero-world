(ns aero-world.db.core
  (:require [datomic.api :as d]
            [buddy.hashers :as hs]))

;; For test porpuses only
(defn create-empty-in-memory-db []
  (let [uri "datomic:mem://aero-world-clojure-db"]
    (d/delete-database uri)
    (d/create-database uri)
    (let [conn (d/connect uri)
          schema (load-file "resources/datomic/schema.edn")]
      (d/transact conn schema)
      conn)))

(def conn (create-empty-in-memory-db))
;;

(defn entity [conn id]
  (d/entity (d/db conn) id))

(defn touch [results conn]
  (let [e (partial entity conn)]
    (map #(-> % first e d/touch) results)))

(defn touch-by-entity-id [entity-id]
  (-> (entity conn entity-id)))

(defn find-airport-by-icao [icao]
  (d/q '[:find ?a
         :where
         [?a :airport/icao ?icao]]
       (d/db conn)
       icao))

(defn find-flight-by-from [from]
  (let [from-entity (find-airport-by-icao from)]
    (first
     (first
      (d/q '[:find ?f
             :where
             [?f :flight/from ?from-entity]]
           (d/db conn)
           from-entity)))))

(defn find-aircraft-by-type [type]
   (first 
    (first
     (d/q '[:find ?a
            :where
            [?a :aircraft/type ?type]]
          (d/db conn)
          type))))

(defn transact! [data]
  @(d/transact conn data))

(defn add-flight-query [{:keys [from to aircraft username]}]
  {:flight/created-at (java.util.Date.)
   :flight/from {:airport/icao from}
   :flight/to {:airport/icao to}
   :flight/aircraft aircraft
   :flight/user {:user/username username}
   :flight/status :flight.status/scheduled})

(defn add-user! [{:keys [username password airport]}]
  @(d/transact conn [{:user/username username 
                      :user/password (hs/encrypt password) 
                      :user/balance 0.0
                      :user/airport {:airport/icao airport}}]))

(defn add-aircraft! [{:keys [register type airport status]}]
  @(d/transact conn [{:aircraft/register register 
                      :aircraft/type type 
                      :aircraft/airport {:airport/icao airport}
                      :aircraft/status status}]))

(defn add-airport! [icao]
  @(d/transact conn [{:airport/icao icao}]))

(defn find-user-by-username [username]
  (-> (d/q '[:find ?u
         :where [?u :user/username ?username]]
       (d/db conn)
       username) (touch conn) first))


(defn find-user-by-id [id]
  (-> id (touch conn) first))

(defn find-all-flights []
  (-> (d/q '[:find ?f
             :where 
             [?f :flight/from ?from]
             [?f :flight/to ?to]
             [?f :flight/aircraft ?aircraft]]
           (d/db conn))
      (touch conn)))

(defn find-all-users []
   (-> (d/q '[:find ?u
              :where
              [?u :user/username]]
            (d/db conn))
       (touch conn)))

(defn find-all-airports []
  (-> (d/q '[:find ?u
             :where
             [?u :airport/icao]]
           (d/db conn))
      (touch conn)))

(defn find-all-aircrafts []
  (-> (d/q '[:find ?aircraft ?type
             :where
             [?aircraft :aircraft/type ?type]]
           (d/db conn))
      (touch conn)))

(defn find-all-available-aircrafts []
  (-> (d/q '[:find ?aircraft ?type ?status
             :where
             [?aircraft :aircraft/type ?type]
             [?aircraft :aircraft/status ?status]]
           (d/db conn))
      (touch conn)))



(defn get-current-user-flight [user-id]
  (if (nil? user-id)
    nil
    (-> (d/q '[:find ?f
               :in $ ?user-id
               :where 
               [?f :flight/user ?user-id]
               (or [?f :flight/status :flight.status/scheduled] [?f :flight/status :flight.status/flying])]
             (d/db conn)
             user-id) (touch conn) first)))

(defn update-flight [{:keys [flight-entity-id flight-status]}]
  @(d/transact conn
   [[:db/add
     flight-entity-id 
     :flight/status 
     flight-status]]))

(defn update-aircraft! [{:keys [register key value]}]
  @(d/transact conn
               [{:aircraft/register register
                 key
                 value}]))

(defn get-order-by-from [from-icao status]
  (if (or (nil? from-icao) (nil? status))
    nil
    (d/q '[:find ?o ?from-icao ?to-icao ?product ?value ?status
           :in $ ?from-icao ?status
           :where
           [?from :airport/icao ?from-icao]
           [?o :order/from ?from]
           [?o :order/status ?status]
           [?o :order/to ?to]
           [?to :airport/icao ?to-icao]
           [?o :order/product ?product]
           [?o :order/value ?value]]
         (d/db conn)
         from-icao
         status)))