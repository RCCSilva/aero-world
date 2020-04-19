(ns aero-world.middleware
  (:require [ring.middleware.format :refer [wrap-restful-format]]))

(defn wrap-formats [handler]
  (let [wrapped (wrap-restful-format
                 handler
                 {:formats [:json-kw :transit-json :transit-msgpack]})]
    (fn [request]
      (wrapped request))))

