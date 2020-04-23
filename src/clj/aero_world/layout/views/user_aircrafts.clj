(ns aero-world.layout.views.user-aircrafts
  (:require [aero-world.layout.components :refer [info-box navbar build-table button]]
            [aero-world.utils :refer [entity->hashmap]]
            [clojure.string :refer [capitalize]]))

(defn build-aircraft-for-table [aircraft]
  
  (let [aircraft-db-id (-> aircraft :db/id)]
   (-> aircraft
       entity->hashmap
       (assoc :aircraft/airport (-> aircraft :aircraft/airport :airport/icao))
       (assoc :aircraft/status (-> aircraft :aircraft/status name capitalize))
       (assoc :aircraft/action (if (-> aircraft :aircraft/available-for-rent?)
                                 (button {:href (str "/aircraft/" aircraft-db-id "/unavailable-for-rent")
                                          :title "Unavailable for Rent"})
                                 (button {:href (str "/aircraft/" aircraft-db-id "/available-for-rent")
                                          :title "Available for Rent"})))
       (dissoc :aircraft/owner))))

(defn user-aircrafts [{:keys [current-user aircrafts]}]
  [:div
   [:div (navbar (-> current-user :user/username))]
   [:div.mt-4.px-16
    [:div.mx-auto {:class "w-1/2"}
     (build-table (map build-aircraft-for-table aircrafts))]]])