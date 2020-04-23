(ns aero-world.layout.views.dashboard
  (:require [aero-world.layout.components :refer [info-box navbar build-table button]]
            [aero-world.utils :refer [entity->hashmap]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn get-button-value [aircraft]
  (case (-> aircraft :aircraft/status)
    :aircraft.status/available (button {:href (str "/aircraft/" (-> aircraft :db/id) "/rent")
                                        :title "Rent It!"})
    :aircraft.status/rented (button {:href (str "/aircraft/" (-> aircraft :db/id) "/leave")
                                     :title "Leave It!"})
    :else (button {:href (str "/aircraft/" (-> aircraft :db/id) "/leave")
                   :title (-> aircraft :aircraft/status)})))

(defn build-aircraft-for-table [aircraft]
  (let [airport (-> aircraft :aircraft/airport :airport/icao)
        aircraft-hashmap (entity->hashmap aircraft)
        button-value (get-button-value aircraft)]
    (-> (assoc aircraft-hashmap :aircraft/airport airport)
        (assoc :aircraft/action button-value)
        (dissoc :aircraft/payload :aircraft/owner :aircraft/available-for-rent?))))

(defn build-payload-for-table [order]
  (let [order-hashmap (entity->hashmap order)]
    (-> order-hashmap
        (assoc :order/from (-> order :order/from :airport/icao))
        (assoc :order/to (-> order :order/to :airport/icao))
        (assoc :order/product (-> order :order/product :product/name)))))

(defn build-available-orders-for-table [order]
  (let [order-hashmap (entity->hashmap order)]
    (-> order-hashmap
        (assoc :order/from (-> order :order/from :airport/icao))
        (assoc :order/to (-> order :order/to :airport/icao))
        (assoc :order/product (-> order :order/product :product/name))
        (assoc :order/action (button {:href (str "/order/" (-> order :db/id) "/assign")
                                      :title "Assign"})))))

(defn flight-box [flight]
  "Receives a flight entity and builds a box with its values"
  (let [aircraft (-> flight :flight/aircraft :aircraft/type)
        from (-> flight :flight/from :airport/icao)
        to (-> flight :flight/to :airport/icao)
        created-at (-> flight :flight/created-at)
        status (-> flight :flight/status)
        flight-id (-> flight :db/id)]
    [:div.flex.flex-row.items-center.justify-between.bg-blue-300.my-2.rounded
     [:div.rounded-lg
      [:div.flex.flex-col.py-2.px-4
       [:span (str "Aircraft: " aircraft)]
       [:span (str "From: " from)]
       [:span (str "To: " to)]
       [:span (str "Start At: " created-at)]]]
     [:div.pr-4 (case status
                  :flight.status/scheduled (button {:href (str "/flight/" flight-id "/start") :title "Start"})
                  :flight.status/flying (button {:href (str "/flight/" flight-id "/finish") :title "Finish"})
                  :flight.status/finished [:div.mx-auto.my-auto.bg-red-500.rounded.px-4.py-2
                                           [:span.text-white.font-bold "Finished"]])]]))

(defn dashboard [{:keys [current-user aircrafts current-flight available-orders]}]
  [:div
   [:div (navbar (-> current-user :user/username))]
   [:div.px-16.mt-8
    [:div.flex.flex-row.justify-between
     [:div (info-box (str "Balance: " (-> current-user :user/balance)))]
     [:div (info-box (str "Current Airport: " (-> current-user :user/airport :airport/icao)))]
     [:div (info-box (format "Current Aircraft: %s (%s)" 
                             (-> current-user :user/aircraft :aircraft/type)
                             (-> current-user :user/aircraft :aircraft/register)))]]
    [:div.mt-2
     [:div.flex.flex-col
      [:div.flex.flex-row
       [:div {:class "w-1/2"}
        [:div.px-2.py-4.rounded.shadow
         [:h2.text-2xl.font-bold "Aircrafts"]
         [:div.mt-4
          (build-table (map build-aircraft-for-table aircrafts))]]]
       [:div {:class "w-1/2"}
        [:div.px-2.py-4.rounded.shadow-lg
         [:h2.text-2xl.font-bold "Create a Flight!"]
         (if current-flight
           (flight-box current-flight)
           [:form {:method :post :action "/flight"}
            (anti-forgery-field)
            [:label {:for "to"} "Where to?"]
            [:input.px.2.py-1.rounded.border.w-full {:type :text :name :to}]
            [:input.px.2.py-1.bg-blue-500.hover:bg-blue-600.rounded.border.w-full.text-white.font-bold
             {:type :submit :value "Start your flight!"}]])]]]
      [:div.flex.mt-4
       [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
        [:h2.text-4xl.font-bold "Current Payload"]
        (build-table (map build-payload-for-table (-> current-user :user/aircraft :aircraft/payload)))]
       [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
        [:h2.text-4xl.font-bold "Available Orders"]
        [:a {:href (str "/airports/" (-> current-user :user/airport :airport/icao) "/create-order")}
         [:button.px-8.py-4.bg-blue-500.hover:bg-blue-600.rounded.border.font-bold.text-white
          "Create Random Order"]]
        (build-table (map build-available-orders-for-table available-orders))]]]]]])