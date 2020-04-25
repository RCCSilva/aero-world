(ns aero-world.middleware
  (:require [ring.util.http-response :as response]))

(defn wrap-auth [handler]
  (fn [request]
    (if (-> request :is-authenticated)
      (handler request)
      (response/found "/login"))))