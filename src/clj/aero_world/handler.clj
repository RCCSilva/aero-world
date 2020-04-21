(ns aero-world.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.http-response :as response]
            [aero-world.layout.core :as layout-core]
            [aero-world.db.core :as db]
            [aero-world.auth :as auth]
            [aero-world.service :as service]))

(defn set-cookie [response cookie-name value]
  (-> response (assoc-in [:cookies cookie-name] value)))

(defn create-auth-token [req]
  (let [[ok? res] (auth/create-auth-token
                   (:params req))]
    (if ok?
      (-> (response/found "/dashboard") (set-cookie :session-token {:value res}))
      (response/found "/login"))))

(defn dashboard-page [request]
  (let [current-user (request :user-entity)
        user-db-id (-> current-user :db/id)]
    (if (request :is-authenticated)
      (layout-core/dashboard-page {:flights (db/find-all-flights)
                                   :aircrafts (db/find-aircrafts-available-for-user user-db-id)
                                   :current-user current-user
                                   :available-orders (db/get-order-by-from (-> current-user :user/airport :airport/icao) :order.status/available)
                                   :current-flight (db/get-current-user-flight user-db-id)})
      (response/found "/login"))))

(defn login-page [request]
  (if (request :is-authenticated)
    (response/found "/dashboard")
    (layout-core/login-page)))

(defn logs-page [request]
  (let [current-user (request :user-entity)
        orders-history (db/get-history-of-flights (current-user :db/id))
        orders-history-value (map #(->
                                    (into {} %)
                                    (assoc
                                     :flight/value
                                     (db/get-order-sums-by-flight (-> % :flight.db/id))))
                                  orders-history)]
    (layout-core/logs-page {:current-user current-user
                  :history-of-flights (sort-by :flight/created-at orders-history-value)})))

(defn airports-page [request]
  (let [current-user (request :user-entity)] 
   (layout-core/airports-page {:current-user current-user
                               :airports (db/find-all-airports)})))

(defn airports-icao-page [request]
  (let [current-user (request :user-entity)
        icao (-> request :route-params :icao)
        available-aircrafts (db/find-aircrafts-by-icao icao)
        available-orders (db/find-orders-by-icao icao)]
    
    (layout-core/sinle-airport-page {:current-user current-user
                                      :available-aircrafts available-aircrafts
                                      :available-orders available-orders})))

(defn login-user [request]
  (create-auth-token request))

(defn logout-user [request]
  (-> (response/found "/") (set-cookie :session-token {:value nil})))

(defn register-page [request]
  (if (request :is-authenticated)
    (response/found "/dashboard")
    (layout-core/register-page)))

(defn register-user [request]
  (let [params (request :params)]
    (if (db/find-user-by-username (params :username))
      (response/found "/register")
      (do
        (db/add-user! params)
        (response/found "/login")))))

(defn add-user! [request]
  (let [params (select-keys (:params request) [:username :password])]
    (db/add-user! params)))

(defn create-flight! [request]
  (let [current-user (-> request :user-entity :db/id db/touch-by-entity-id)
        current-user-db-id (-> current-user :db/id)
        aircraft (-> current-user :user/aircraft)
        aircraft-db-id (-> aircraft :db/id)
        aircraft-airport (-> aircraft :aircraft/airport :airport/icao)
        flight-to (-> request :params :to)  
        query (service/create-flight-query {:from aircraft-airport
                                           :to flight-to
                                           :aircraft aircraft-db-id
                                           :user current-user-db-id})]
    (when (and (db/find-airport-by-icao flight-to) (not (nil? aircraft-db-id)))
      (db/transact! query))
    (response/found "/dashboard")))

(defn finish-flight! [flight-id]
  (let  [flight (db/touch-by-entity-id flight-id)
         closed-at (java.util.Date.)
         query (service/finish-flight-query {:flight flight
                                             :closed-at closed-at})]
    (db/transact! query))
  (response/found "/dashboard"))

  (defn start-flight! [flight-id]
    (db/update-flight {:flight-entity-id flight-id :flight-status :flight.status/flying})
    (response/found "/dashboard"))

(defn rent-aircraft! [request]
  (let [user (-> request :user-entity :db/id Long. db/touch-by-entity-id)
        aircraft (-> request :route-params :id Long. db/touch-by-entity-id)
        query (service/rent-aircraft-query {:aircraft aircraft :user user})]
    (when (= (-> user :user/airport) (-> aircraft :aircraft/airport))
      (db/transact! query)))
    (response/found "/dashboard"))

(defn leave-aircraft! [request]
  (let [user (-> request :user-entity)
        aircraft (-> request :route-params :id Long. db/touch-by-entity-id)
        query (service/leave-aircraft-query {:user user
                                              :aircraft aircraft})]
    (db/transact! query))
  (response/found "/dashboard"))

(defn assign-order! [request]
  (let [order (-> request :route-params :id Long. db/touch-by-entity-id)
        aircraft (-> request :user-entity :user/aircraft)
        query (service/assign-order-query {:aircraft aircraft 
                                         :order order})]
    (db/transact! query))
  (response/found "/dashboard"))

(defn deliver-order! [request]
  (let [current-user (-> request :user-entity :db/id db/touch-by-entity-id)
        order-db-id (-> request :route-params :id Long.)
        aircraft (-> current-user :user/aircraft)
        order (db/touch-by-entity-id order-db-id)
        query (service/deliver-one-order-query {:aircraft aircraft
                                                :order order
                                                :user current-user})]
    (when (= (-> aircraft :aircraft/airport) (-> order :order/to))
      (db/transact! query)))
  (response/found "/dashboard"))

(defn create-random-order! [request]
  (let [icao (-> request :route-params :icao)
        airport (-> icao db/find-airport-by-icao db/touch-by-entity-id)
        product (-> (db/find-all-products) vec service/get-random)
        query (service/create-order-random-query {:airport airport
                                                  :product product})]
    
    (db/transact! query))
  (response/found "/dashboard"))

(defn my-aircrafts [request]
  (let [current-user (request :user-entity)
        user-aircrafts (db/find-aircrafts-by-owner (-> current-user :db/id))]
    (layout-core/user-aircrafts-page {:current-user current-user
                                      :aircrafts user-aircrafts})))

(defn make-aircraft-available-for-rent! [request]
  (let [aircraft (-> request :route-params :id Long. db/touch-by-entity-id)
        query (service/make-aircraft-available-for-rent-query aircraft)]
    (db/transact! query))
  (response/found "/my-aircrafts"))

(defn make-aircraft-unavailable-for-rent! [request]
  (let [aircraft (-> request :route-params :id Long. db/touch-by-entity-id)
        query (service/make-aircraft-unavailable-for-rent-query aircraft)]
    (db/transact! query))
  (response/found "/my-aircrafts"))

(defn home-page [request]
  (layout-core/home-page))

(defroutes app-routes
  (GET "/" [] home-page)
  (GET "/dashboard" [] dashboard-page)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/login" [] login-page)
  (POST "/login" [] login-user)
  (GET "/logout" [] logout-user)
  (POST "/flight" [] create-flight!)
  (GET "/logs" [] logs-page)
  (GET "/airports" [] airports-page)
  (GET "/airports/:icao" [] airports-icao-page)
  (GET "/airports/:icao/create-order" [] create-random-order!)
  (GET "/flight/:id/start" [id] (start-flight! (Long. id)))
  (GET "/flight/:id/finish" [id] (finish-flight! (Long. id)))
  (GET "/aircraft/:id/rent" [] rent-aircraft!)
  (GET "/aircraft/:id/leave" [] leave-aircraft!)
  (GET "/aircraft/:id/available-for-rent" [] make-aircraft-available-for-rent!)
  (GET "/aircraft/:id/unavailable-for-rent" [] make-aircraft-unavailable-for-rent!)
  (GET "/order/:id/assign" [] assign-order!)
  (GET "/order/:id/deliver" [] deliver-order!)
  (GET "/my-aircrafts" [] my-aircrafts)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      auth/authentication-middleware
      (wrap-defaults  site-defaults)))
