(ns aero-world.layout.components
  (:require [clojure.string :refer [capitalize]]))

(defn info-box [text]
  [:div.bg-blue-500.px-8.py-4.rounded
   [:span.font-bold.text-white
    text]])

(defn navbar [username]
  [:div.px-16.py-2.bg-white.shadow-lg
   [:div.flex.flex-row.items-center.justify-between
    [:div.flex.flex-row.items-center
     [:a {:href "/dashboard"}
      [:span.text-2xl.font-bold.text-gray-800 "Aero World"]]
     [:div.ml-4
      [:a {:href "/airports"}
       [:span.mx-2.text-xl.hover:font-bold.text-gray-800.cursor-pointer "Airports"]]
      [:a {:href "/my-aircrafts"}
       [:span.mx-2.text-xl.hover:font-bold.text-gray-800.cursor-pointer "My Aircrafts"]]
      [:a {:href "/buy-aircrafts"}
       [:span.mx-2.text-xl.hover:font-bold.text-gray-800.cursor-pointer "Buy"]]
      [:a {:href "/logs"}
       [:span.mx-2.text-xl.hover:font-bold.text-gray-800.cursor-pointer "Logs"]]]]
    [:div
     [:span.pr-8.font-bold.text-gray-800 (str "Hello, " username)]
     [:a {:href "/logout"}
      [:button.bg-red-500.hover:bg-red-600.rounded.px-4.py-2.font-bold.text-white  "Logout"]]]]])

(defn build-table [data]
  (let [columns (keys (first data))]
    [:table.datatable
     [:thead
      [:tr
       (for [head columns]
         [:th (capitalize (name head))])]]
     [:tbody
      (for [row data]
        [:tr
         (for [column columns]
           [:td (-> row column)])])]]))

(defn button [{:keys [href title]}]
  [:a {:href href}
   [:button.px-4.py-2.bg-teal-600.hover:bg-teal-700.rounded.
    [:span.mx-auto.text-white.font-bold title]]])