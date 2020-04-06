(ns aero-world.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.http-response :as response]
            [aero-world.layout :as layout]
            [aero-world.db.core :as db]
            [aero-world.auth :as auth]))

(defn set-cookie [response cookie-name value]
  (-> response (assoc-in [:cookies cookie-name] value)))

(defn create-auth-token [req]
  (let [[ok? res] (auth/create-auth-token
                   (:params req))]
    (if ok?
      (-> (response/found "/") (set-cookie :session-token {:value res}))
      (response/found "/login"))))

(defn home-page [request]
  (let [current-user (request :user-entity)]
    (if (request :is-authenticated)
      (layout/dashboard {:flights (db/find-all-flights)
                         :aircrafts (db/find-all-available-aircrafts)
                         :current-user current-user
                         :available-orders (db/get-order-by-from (-> current-user :user/airport :airport/icao) :order.status/available)
                         :current-flight (db/get-current-user-flight (-> current-user :db/id))})
      (response/found "/login"))))

(defn login-page [request]
  (if (request :is-authenticated)
    (response/found "/")
    (layout/login)))

(defn login-user [request]
  (create-auth-token request))

(defn logout-user [request]
  (-> (response/found "/") (set-cookie :session-token {:value nil})))

(defn register-page [request]
    (if (request :is-authenticated)
    (response/found "/")
    (layout/register)))

(defn register-user [request]
  (let [params (request :params)]
    (db/add-user! params)
    (response/found "/login")))

(defn add-user! [request]
  (let [params (select-keys (:params request) [:username :password])]
    (db/add-user! params)))

(defn create-flight! [request]
  (let [current-user (-> request :user-entity :db/id db/touch-by-entity-id)
        aircraft (-> current-user :user/aircraft)
        aircraft-db-id (-> aircraft :db/id)
        aircraft-airport (-> aircraft :aircraft/airport :airport/icao)
        flight-to (-> request :params :to)
        username (-> request :user-entity :user/username)
        data {:from aircraft-airport
              :to flight-to
              :aircraft aircraft-db-id
              :username username}
        query (db/add-flight-query data)]
    (println data)
    (db/transact! [query])
    (response/found "/")))

(defn finish-flight! [flight-id]
  (let  [flight (db/touch-by-entity-id flight-id)
         aircraft-db-id (-> flight :flight/aircraft :db/id)
         airport-db-id (-> flight :flight/to :db/id)
         user-db-id (-> flight :flight/user :db/id)
         update-flight-status [:db/add flight-id :flight/status :flight.status/finished]
         update-aircraft [:db/add aircraft-db-id :aircraft/airport airport-db-id]
         update-user [:db/add user-db-id :user/airport airport-db-id]]
    (println [ update-flight-status update-aircraft update-user])
    (db/transact! [update-flight-status update-aircraft update-user]))
  (response/found "/"))


(defn start-flight! [flight-id]
  (db/update-flight {:flight-entity-id flight-id :flight-status :flight.status/flying})
  (response/found "/"))

(defn rent-aircraft! [request]
  (let [user-db-id (-> request :user-entity :db/id Long.)
        aircraft-db-id (-> request :route-params :id Long.)]
   (db/transact! [[:db/add user-db-id
                   :user/aircraft aircraft-db-id]
                  [:db/add aircraft-db-id
                   :aircraft/status :aircraft.status/rented]]))
  (response/found "/"))

(defn leave-aircraft! [request]
  (let [user-db-id (-> request :user-entity :db/id Long.)
        aircraft-db-id (-> request :route-params :id Long.)]
    (db/transact! [[:db/retract user-db-id
                    :user/aircraft aircraft-db-id]
                   [:db/add aircraft-db-id
                    :aircraft/status :aircraft.status/available]]))
  (response/found "/"))

(defn assign-order! [request]
  (let [order-db-id (-> request :route-params :id Long.)
        aircraft-db-id (-> request :user-entity :user/aircraft :db/id)]
    (db/transact! [[:db/add aircraft-db-id :aircraft/payload order-db-id]
                   [:db/add order-db-id :order/status :order.status/assigned]]))
  (response/found "/"))

(defn deliver-order! [request]
  (let [current-user (-> request :user-entity :db/id db/touch-by-entity-id)
        current-user-db-id (-> current-user :db/id)
        order-db-id (-> request :route-params :id Long.)
        aircraft-db-id (-> current-user :user/aircraft :db/id)
        order (db/touch-by-entity-id order-db-id)
        value (-> order :order/value)
        current-user-balance (-> request :user-entity :user/balance)
        new-balance (+ value current-user-balance)]
    (db/transact! [[:db/retract aircraft-db-id :aircraft/payload order-db-id]
                   [:db/add order-db-id :order/status :order.status/delivered]
                   [:db/add current-user-db-id :user/balance new-balance]]))
  (response/found "/"))

(defroutes app-routes
  (GET "/" [] home-page)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/login" [] login-page)
  (POST "/login" [] login-user)
  (GET "/logout" [] logout-user)
  (POST "/flight" [] create-flight!)
  (GET "/flight/:id/start" [id] (start-flight! (Long. id)))
  (GET "/flight/:id/finish" [id] (finish-flight! (Long. id)))
  (GET "/aircraft/:id/rent" [] rent-aircraft!)
  (GET "/aircraft/:id/leave" [] leave-aircraft!)
  (GET "/order/:id/assign" [] assign-order!)
  (GET "/order/:id/deliver" [] deliver-order!)
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      auth/authentication-middleware
      (wrap-defaults  site-defaults)))
