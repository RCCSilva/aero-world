(ns aero-world-clojure.core
   (:require [ring.adapter.jetty :refer [run-jetty]]
             [aero-world-clojure.handler :refer [app]]
             [environ.core :refer [env]]
             [ring.middleware.reload :refer [wrap-reload]]))
 
(defn -main [& port]
  (let [port (Integer. (or port (env :port) 3000))]
    (run-jetty app {:port port :join? false})))


(defn -dev-main [& port]
  (let [port (Integer. (or port (env :port) 3000))]
    (run-jetty (wrap-reload #'app) {:port port :join? false})))
