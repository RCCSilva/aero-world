(ns aero-world.layout.views.login
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn login [params]
  [:div.flex
   [:div..mx-auto.w-64.flex.flex-col
    [:h1.font-bold.text-gray-900.text-xl "Login"]
    [:form.flex.flex-col {:method :post :action "/login"}
     (anti-forgery-field)
     [:label "Username"]
     [:input.border.rounded.px-2.py-1 {:type :text :name :username}]
     [:label "Password"]
     [:input.border.rounded.px-2.py-1 {:type :password :name :password}]
     [:input.py-2.mt-2.bg-blue-500.hover:bg-blue-600.border.rounded-lg.cursor-pointer.font-bold.text-white 
      {:type :submit :value "Login"}]]
    [:a {:href "/register"}
     [:button.w-full.mt-2.py-2.bg-blue-500.hover:bg-blue-600.border.rounded-lg.font-bold.text-white "Register"]]]])