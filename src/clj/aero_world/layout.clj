(ns aero-world.layout
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]]
            [clojure.string :as str])
  (:use hiccup.core)
  (:use hiccup.page))

(defn button [{:keys [href title]}]
  [:div.bg-teal-600.hover:bg-teal-700.rounded
   [:a {:href href}
    [:button.px-4.py-2.text-white.font-bold title]]])

(defn head []
   [:head
    [:link {:href "https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" 
            :rel "stylesheet"}]])


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

(defn user-box [user]
  "Receives a flight entity and builds a box with its values"
  (let [username (-> user :user/username)
        balance (-> user :user/balance)
        id (-> user :db/id)]
    [:div.flex.flex-col.my-2.rounded
    [:div.bg-blue-300.rounded-lg
      [:div.flex.flex-col.py-2.px-4
      [:span (str "User ID: " id)]
      [:span (str "Username: " username)]
      [:span (str "Balance: " balance)]]]]))

(defn create-options [{:keys [value text]}]
  [:option {:value value} text])

(defn create-aircraft-select-row [aircraft]
  [:div.px-2.py-2.bg-blue-500.rounded 
   [:div.flex.flex-row.justify-around.items-center
    [:span.text-white.font-bold (-> aircraft :aircraft/register)]
    [:span.text-white.font-bold (-> aircraft :aircraft/type)]
    [:span.text-white.font-bold (-> aircraft :aircraft/airport :airport/icao)]
     (case (-> aircraft :aircraft/status)
       :aircraft.status/available (button {:href (str "/aircraft/" (-> aircraft :db/id) "/rent") 
                                           :title "Rent It!"})
       :aircraft.status/rented (button {:href (str "/aircraft/" (-> aircraft :db/id) "/leave") 
                                        :title "Leave It!"}))
     ] 
    ])

(defn create-available-orders-select-row [order]
  [:div.my-1.px-2.py-2.bg-blue-500.rounded
   [:div.flex.flex-row.justify-around.items-center
    [:span.text-white.font-bold (-> order :order/from :airport/icao)]
    [:span.text-white.font-bold (-> order :order/to :airport/icao)]
    [:span.text-white.font-bold (-> order :order/product :product/name)]
    [:span.text-white.font-bold (-> order :order/value)]
    [:a {:href (str "/order/" (-> order :db/id) "/assign")}
     [:button.px-2..py-1.bg-teal-700.hover:bg-teal-800.rounded.text-white.font-bold "Assign"]]]])

(defn create-payload-select-row [payload]
  [:div.px-2.py-2.bg-blue-500.rounded
   [:div.flex.flex-row.justify-around.items-center
    [:span.text-white.font-bold (-> payload :order/from :airport/icao)]
    [:span.text-white.font-bold (-> payload :order/to :airport/icao)]
    [:a {:href (str "/order/" (-> payload :db/id) "/deliver")}
     [:button.px-2..py-1.bg-teal-700.hover:bg-teal-800.rounded.text-white.font-bold "Deliver!"]]]])

(defn navbar [current-user]
  [:div.px-16.py-2.bg-white.shadow-lg
   [:div.flex.flex-row.items-center.justify-between
    [:div.flex.flex-row.items-center
     [:a {:href "/"}
      [:span.text-2xl.font-bold.text-gray-800 "Aero World"]]
     [:div.ml-4
      [:a {:href "/airports"}
       [:span.mx-2.text-xl.hover:font-bold.text-gray-800.cursor-pointer "Airports"]]
      [:a {:href "/logs"}
       [:span.mx-2.text-xl.hover:font-bold.text-gray-800.cursor-pointer "Logs"]]]]
    [:div
     [:span.pr-8.font-bold.text-gray-800 (str "Hello, " (current-user :user/username))]
     [:a {:href "/logout"}
      [:button.bg-red-500.hover:bg-red-600.rounded.px-4.py-2.font-bold.text-white  "Logout"]]]]])

(defn flight-history-box [flight]
  [:div.mt-2.px-2.py-2.bg-blue-200.rounded
   [:div.flex.flex-row.items-center.justify-around
    [:span (flight :flight/created-at)]
    [:span (flight :aircraft/type)]
    [:span (flight :aircraft/register)]
    [:span (flight :flight/from-icao)]
    [:span (flight :flight/to-icao)]
    [:span (flight :flight/value)]]])

(defn logs [{:keys [current-user history-of-flights]}]
  (html5
   (head)
   (navbar current-user)
   [:div.flex.flex-col
    [:div.mt-32.mx-auto {:class "w-1/2"}
     (if history-of-flights
       (map flight-history-box history-of-flights)
       nil)]]))

(defn airport-box [airport]
  [:a {:href (str "/airports/" (-> airport :airport/icao))}
   [:div.mt-2.px-2.py-2.bg-blue-200.rounded
    [:div.flex.flex-row.items-center.justify-around
     (-> airport :airport/icao)]]])

(defn airports [{:keys [current-user airports] }]
  (html5
   (head)
   (navbar current-user)
   [:div.flex.flex-col
    [:div.mt-32.mx-auto {:class "w-1/2"}
     (map airport-box airports)]]))

(defn order-box [order]
  [:div.mt-2.mx-8.px-2.py-2.bg-blue-300.rounded-lg
   [:div.flex.flex-row.justify-around 
    [:span (-> order :order/from :airport/icao)]
    [:span (-> order :order/to :airport/icao)]
    [:span (-> order :order/product)]
    [:span (-> order :order/value)]]])

(defn aircraft-box [aircraft]
  [:div.mt-2.mx-8.px-2.py-2.bg-blue-300.rounded-lg
   [:div.div.flex.flex-row.justify-around
    [:span (-> aircraft :aircraft/register)]
    [:span (-> aircraft :aircraft/type)]
    [:span (-> aircraft :aircraft/status)]]])

(defn airports-single-page [{:keys [current-user available-aircrafts available-orders]}]
  (html5
   (head)
   (navbar current-user)
   [:div.px-16.mt-8
    [:div.flex.flex-row
     [:div {:class "w-1/2"}
      [:div
       [:h2.text-gray-800.text-2xl.font-bold "Available Aircrafts"
        [:div (map aircraft-box available-aircrafts)]]]]
     [:div {:class "w-1/2"}
      [:div
       [:h2.text-gray-800.text-2xl.font-bold "Available Orders"
        [:div (map order-box available-orders)]]]]]]))

(defn dashboard [{:keys [aircrafts current-user available-orders current-flight]}]
  (html5
   (head)
   (navbar current-user)
   [:div.flex.flex-row
    [:div.bg-blue-500.ml-16.mt-6.px-8.py-4.rounded {:class "w-1/6"}
     [:span.font-bold.text-white
      (str "Balance: " (current-user :user/balance))]]
    [:div.bg-blue-500.ml-16.mt-6.px-8.py-4.rounded {:class "w-1/6"}
     [:span.font-bold.text-white
      (str "Current Airport: " (-> current-user :user/airport :airport/icao))]]
    [:div.bg-blue-500.ml-16.mt-6.px-8.py-4.rounded {:class "w-1/6"}
     [:span.font-bold.text-white
      (if (-> current-user :user/aircraft :aircraft/register)
        (format "Current Aircraft: %s (%s)"
                (-> current-user :user/aircraft :aircraft/register)
                (-> current-user :user/aircraft :aircraft/type))
        "You don't have aircraft")]]]
   [:div.flex.flex-col.px-16
    [:div.flex
     [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
      [:h2.text-4xl.font-bold "Available Aircrafts"]
      [:div (map create-aircraft-select-row aircrafts)]]
     [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
      [:h2.text-4xl.font-bold "Fly"]
      (if current-flight
        (flight-box current-flight)
        [:form {:method :post :action "/flight"}
         (anti-forgery-field)
         [:label {:for "to"} "Where to?"]
         [:input.px.2.py-1.rounded.border.w-full {:type :text :name :to}]
         [:input.px.2.py-1.bg-blue-500.hover:bg-blue-600.rounded.border.w-full.text-white.font-bold
          {:type :submit :value "Start your flight!"}]])]]
    [:div.flex.mt-4
     [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
      [:h2.text-4xl.font-bold "Current Payload"]
      (map create-payload-select-row (-> current-user :user/aircraft :aircraft/payload))]
     [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
      [:h2.text-4xl.font-bold "Available Orders"]
      [:a {:href (str "/airports/" (-> current-user :user/airport :airport/icao) "/create-order")}
       [:button.px-8.py-4.bg-blue-500.hover:bg-blue-600.rounded.border.font-bold.text-white 
        "Create Random Order"] ]
      (map create-available-orders-select-row available-orders)]]]))

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
      [:button.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg "Register"]]]))

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