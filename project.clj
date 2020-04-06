(defproject aero-world "0.1.0-SNAPSHOT"
  :description "Aero World"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring "1.8.0"]
                 [com.datomic/datomic-free "0.9.5697"]
                 [http-kit "2.3.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [environ "1.1.0"]
                 [buddy "2.0.0"]]
  :plugins [[lein-ring "0.12.5"]]
  :main aero-world.core
  :ring {:handler aero-world.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]
         :main aero-world.core/-dev-main}}
  :uberjar-name "aero-world-standalone.jar"
  :repl-options {:init-ns aero-world.core})
