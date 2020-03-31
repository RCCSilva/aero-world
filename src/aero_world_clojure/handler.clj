(ns aero-world-clojure.handler
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.http-response :as response]
            [aero-world-clojure.layout :as layout]
            [aero-world-clojure.db.core :as db]
            [aero-world-clojure.auth :as auth]))

(defn set-cookie [response cookie-name value]
  (-> response (assoc-in [:cookies cookie-name] value)))

(defn create-auth-token [req]
  (let [[ok? res] (auth/create-auth-token
                   (:params req))]
    (if ok?
      (-> (response/found "/") (set-cookie :session-token {:value res}))
      (response/found "/login"))))

(defn home-page [request]
  (if (request :is-authenticated)
    (layout/dashboard {:flights (db/find-all-flights) :current-user (request :user-entity)})
    (response/found "/login")))

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

(defn create-flight [request]
  (let [flight-params (request :params)
        username (-> request :user-entity :user/username)]
    (db/add-flight! (conj flight-params {:username username}))
    (response/found "/")))

(defn finish-flight! [flight-id]
  (db/finish-flight flight-id)
  (db/update-flight {:flight-entity-id flight-id :flight-status :flight.status/finished})
  (response/found "/"))

(defn start-flight! [flight-id]
  (db/update-flight {:flight-entity-id flight-id :flight-status :flight.status/flying})
  (response/found "/"))

(defroutes app-routes
  (GET "/" [] home-page)
  (GET "/register" [] register-page)
  (POST "/register" [] register-user)
  (GET "/login" [] login-page)
  (POST "/login" [] login-user)
  (GET "/logout" [] logout-user)
  (POST "/flight" [] create-flight)
  (GET "/flight/:id/start" [id] (start-flight! (Long. id)))
  (GET "/flight/:id/finish" [id] (finish-flight! (Long. id)))
  (route/not-found "Not Found"))

(def app
  (-> app-routes
      auth/authentication-middleware
      (wrap-defaults  site-defaults)))
