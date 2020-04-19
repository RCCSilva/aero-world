(ns aero-world.layout.views.logs
  (:require [aero-world.layout.components :refer [build-table navbar]]))

(defn logs [{:keys [current-user history-of-flights]}]
  [:div
   [:div (navbar (-> current-user :user/username))]
   [:div.flex.flex-col
    [:div.mt-32.mx-auto {:class "w-1/2"}
     (if history-of-flights
       (build-table history-of-flights)
       nil)]]])