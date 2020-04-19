(ns aero-world.layout.core
  (:require
   [hiccup.page :refer [html5 include-js]]
   [aero-world.layout.views.home :refer [home]]
   [aero-world.layout.views.login :refer [login]]
   [aero-world.layout.views.register :refer [register]]
   [aero-world.layout.views.dashboard :refer [dashboard]]
   [aero-world.layout.views.airports :refer [airports]]
   [aero-world.layout.views.single-airport :refer [single-airport]]
   [aero-world.layout.views.logs :refer [logs]]))

(defn include-css [href]
  [:link {:type "text/css", :href href, :rel "stylesheet"}])

(defn head []
  [:head
   [:title "Aero World"]
   [:meta {:name "description" :content "Aero World"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   (include-css "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css")
   (include-css "https://cdn.datatables.net/1.10.20/css/jquery.dataTables.min.css")])

(defn base-page [body params]
  (html5
   (head)
   (body params)
   (include-js "https://code.jquery.com/jquery-3.4.1.slim.min.js")
   (include-js "https://cdn.datatables.net/1.10.20/js/jquery.dataTables.min.js")
   (include-js "/test.js")))

(defn home-page []
  (base-page home nil))

(defn login-page []
  (base-page login nil))

(defn register-page []
  (base-page register nil))

(defn dashboard-page [params]
  (base-page dashboard params))

(defn airports-page [params]
  (base-page airports params))

(defn sinle-airport-page [params]
  (base-page single-airport params))

(defn logs-page [params]
  (base-page logs params))