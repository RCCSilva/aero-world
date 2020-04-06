(ns aero-world.auth
  (:require
   [buddy.sign.jwt :as jwt]
   [clj-time.core :as t]
   [buddy.hashers :as hs]
   [aero-world.db.core :as db]))

(def secret "my-secret")

(defn convert-datomic-entity-to-map [entity]
  (conj {:db/id (-> entity :db/id)} (into {} entity)))

(defn parse-session-token [token]
  (try
    {:user-entity (-> (jwt/unsign token secret) 
                      :user 
                      :user/username 
                      db/find-user-by-username 
                      convert-datomic-entity-to-map
                      (dissoc :user/password))
     :is-authenticated true}
    (catch Exception e
      (println (str "Caught an exception: " (.getMessage e)))
      {:is-authenticated false})))

(defn authentication-middleware [handler]
   (fn [request]
     (let [session-token (-> request :cookies (get "session-token") :value)]
       (-> request (conj (parse-session-token session-token)) handler))))

(defn auth-user [credentials]
  (let [user (db/find-user-by-username (:username credentials))
        unauthed [false {:message "Invalid username or password"}]]
    (if user
      (if (hs/check (:password credentials) (:user/password user))
        [true {:user ((into {} user) :user/username)}]
        unauthed)
      unauthed)))

(defn create-auth-token [credentials]
  (let [[ok? res] (auth-user credentials)
        exp (-> (t/plus (t/now) (t/days 1)))]
    (if ok?
      [true (jwt/sign (assoc res :exp exp)
                              secret)]
      [false res])))