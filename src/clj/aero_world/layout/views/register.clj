(ns aero-world.layout.views.register
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn register [params]
  [:div.flex.flex-col
   [:div.flex
    [:div.mx-auto.w-64.flex.flex-col
     [:h1.font-bold.text-gray-900.text-xl "Register"]
     [:form.flex.flex-col {:method :post :action "/register"}
      (anti-forgery-field)
      [:label "Username"]
      [:input.border.rounded.px-2.py-1 {:type :text :name :username}]
      [:label "Password"]
      [:input.border.rounded.px-2.py-1 {:type :password :name :password}]
      [:label "Base Airport (ICAO)"]
      [:input.border.rounded.px-2.py-1 {:type :text :name :airport}]
      [:input.py-2.mt-2.bg-blue-500.hover:bg-blue-600.border.rounded-lg.cursor-pointer.font-bold.text-white
       {:type :submit :value "Register"}]]
     [:a {:href "/login"}
      [:button.w-full.mt-2.py-2.bg-blue-500.hover:bg-blue-600.border.rounded-lg.font-bold.text-white "Login"]]]]]
  )