(ns aero-world.layout.views.airports
  (:require [aero-world.utils :refer [entity->hashmap]]
            [aero-world.layout.components :refer [navbar build-table button]]))


(defn build-airport-for-table [airport]
  (-> airport
      entity->hashmap
      (assoc :airport/available-orders (-> airport :airport/available-orders count))
      (assoc :airport/action (button {:href (str "/airports/" (-> airport :airport/icao))
                                      :title "Show"}))))

(defn airports [{:keys [current-user airports]}]
  [:div
   [:div (navbar (-> current-user :user/username))]
   [:div.flex.flex-col
    [:div.mt-32.mx-auto {:class "w-1/2"}
     (build-table (map build-airport-for-table airports))]]])
