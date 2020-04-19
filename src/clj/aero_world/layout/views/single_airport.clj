(ns aero-world.layout.views.single-airport
  (:require [aero-world.utils :refer [entity->hashmap]]
            [aero-world.layout.components :refer [navbar build-table button]]))


(defn single-airport [{:keys [current-user available-aircrafts available-orders]}]
  [:div
   [:div (navbar (-> current-user :user/username))]
   [:div.px-16.mt-8
    [:div.flex.flex-row
     [:div {:class "w-1/2"}
      [:div
       [:h2.text-gray-800.text-2xl.font-bold "Available Aircrafts"]
       [:div (build-table available-aircrafts)]]]
     [:div {:class "w-1/2"}
      [:div
       [:h2.text-gray-800.text-2xl.font-bold "Available Orders"]
       [:div (build-table available-orders)]]]]]])