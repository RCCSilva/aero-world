(ns aero-world-clojure.db.core
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

(defn add-flight! [{:keys [from to aircraft username]}]
  (let [data [{:flight/created-at (java.util.Date.)
              :flight/from {:airport/icao from}
              :flight/to {:airport/icao to}
              :flight/aircraft {:aircraft/type aircraft}
              :flight/user {:user/username username}
              :flight/status :flight.status/scheduled}]]
       (println data)
       @(d/transact conn data)))
  

(defn add-user! [{:keys [username password]}]
  @(d/transact conn [{:user/username username :user/password (hs/encrypt password) :user/balance 0.0}]))

(defn add-aircraft [type]
  @(d/transact conn [{:aircraft/type type}]))

(defn add-airport [icao]
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

(defn finish-flight [flight-id]
  (let [user (-> (entity conn flight-id) :flight/user)
        user-entity-id (-> user :db/id)
        user-current-balance (-> user :user/balance)
        user-new-balance (+ user-current-balance 10.0)]
    @(d/transact conn [[:db/add user-entity-id :user/balance user-new-balance]])))

(defn update-flight [{:keys [flight-entity-id flight-status]}]
  @(d/transact conn
   [[:db/add
     flight-entity-id 
     :flight/status 
     flight-status]]))