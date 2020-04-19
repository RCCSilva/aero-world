(ns aero-world.layout
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.string :as str]
            [hiccup.core :refer [html]]
            [hiccup.page :refer [html5 include-js]]))

(defn include-css [href]
  [:link {:type "text/css", :href href, :rel "stylesheet"}])


(defn head []
   [:head
    (include-css "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css")])

(defn login []
  (html5
   (head)
   [:div.flex.flex-col
    [:form.flex.flex-col {:method :post :action "/login"}
     (anti-forgery-field)
     [:label "Username"]
     [:input.border.rounded.px-2.py-1 {:type :text :name :username}]
     [:label "Password"]
     [:input.border.rounded.px-2.py-1 {:type :password :name :password}]
     [:input.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg {:type :submit :value "Login"}]]
    [:a {:href "/register"}
     [:button.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg "Register"]]]
   (include-js "/js/app.js")))

(defn register []
  (html5
   (head)
   [:div.flex.flex-col
    [:form.flex.flex-col {:method :post :action "/register"}
     (anti-forgery-field)
     [:label "Username"]
     [:input.border.rounded.px-2.py-1 {:type :text :name :username}]
     [:label "Password"]
     [:input.border.rounded.px-2.py-1 {:type :password :name :password}]
     [:label "Base Airport (ICAO)"]
     [:input.border.rounded.px-2.py-1 {:type :text :name :airport}]
     [:input.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg {:type :submit :value "Register"}]]
    [:a.w-full {:href "/login"}
     [:buton.w-full.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg "Login"]]]))