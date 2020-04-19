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
                 [com.datomic/datomic-free "0.9.5697" :exclusions [com.google.guava/guava]]
                 [http-kit "2.3.0"]
                 [metosin/ring-http-response "0.9.1"]
                 [environ "1.1.0"]
                 [buddy "2.0.0"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [org.clojure/tools.reader "1.2.2"]
                 [reagent "0.10.0"]
                 [cljs-ajax "0.5.2"]
                 [ring/ring-json "0.5.0"]
                 [hiccup "1.0.5"]]

  :plugins [[lein-ring "0.12.5"]
            [lein-cljsbuild "1.1.8"]]

  :resource-paths ["resources" "target/cljsbuild"]

  :source-paths ["src/clj"]

  :target-path "target/%s/"
  :cljsbuild
  {:builds {:app {:source-paths ["src/cljs"]
                  :compiler {:output-to     "target/cljsbuild/public/js/app.js"
                             :output-dir    "target/cljsbuild/public/js/out"
                             :main "aero-world.core"
                             :asset-path "/js/out"
                             :optimizations :none
                             :source-map true
                             :pretty-print  true}}}}
  :main aero-world.core

  :ring {:handler aero-world.handler/app}

  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]
         :main aero-world.core/-dev-main}}

  :uberjar {:omit-source true
            :aot :all
            :uberjar-name "aero-world-standalone.jar"
            :source-paths ["env/prod/clj"]
            :resource-paths ["env/prod/resources"]}

  :repl-options {:init-ns aero-world.core})
