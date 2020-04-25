(ns aero-world.core
   (:require [ring.adapter.jetty :refer [run-jetty]]
             [aero-world.handler :refer [main-routes]]
             [environ.core :refer [env]]
             [ring.middleware.reload :refer [wrap-reload]]))
 
(defn -main [& port]
  (let [port (Integer. (or port (env :port) 3000))]
    (run-jetty main-routes {:port port :join? false})))


(defn -dev-main [& port]
  (let [port (Integer. (or port (env :port) 3000))]
    (run-jetty (wrap-reload #'main-routes) {:port port :join? false})))
