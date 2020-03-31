(ns aero-world-clojure.layout
  (:require [ring.util.anti-forgery :refer [anti-forgery-field]])
  (:use hiccup.core)
  (:use hiccup.page))

(defn button [{:keys [href title]}]
  [:div.mx-auto.my-auto.bg-teal-600.hover:bg-teal-700.rounded
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
     [:div.flex.bg-blue-300.my-2.rounded
      [:div.rounded-lg
       [:div.flex.flex-col.py-2.px-4
        [:span (str "Aircraft: " aircraft)]
        [:span (str "From: " from)]
        [:span (str "To: " to)]
        [:span (str "Start At: " created-at)]]]
      (case status
        :flight.status/scheduled (button {:href (str "/flight/" flight-id "/start") :title "Start"})
        :flight.status/flying (button {:href (str "/flight/" flight-id "/finish") :title "Finish"})
        :flight.status/finished [:div.mx-auto.my-auto.bg-red-500.rounded.px-4.py-2 
                                 [:span.text-white.font-bold "Finished"]])]))

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


(defn dashboard [{:keys [flights current-user]}]
  (html5
   (head)
   [:div.px-16.py-2.bg-white.shadow-lg
    [:div.flex.flex-row.items-center.justify-between
     [:div [:span.text-xl.font-bold.text-gray-800 "Aero World"]]
     [:div 
      [:span.pr-8.font-bold.text-gray-800 (str "Hello, " (current-user :user/username))]
      [:button.bg-red-500.hover:bg-red-600.rounded.px-4.py-2 [:a.font-bold.text-white {:href "/logout"} "Logout"]]]]]
   [:div.bg-blue-500.ml-16.mt-6.px-8.py-4.rounded {:class "w-1/6"}
    [:span.font-bold.text-white
     (str "Balance: " (current-user :user/balance))]]
   [:div.flex.px-16
    [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
     [:h2.text-4xl.font-bold "Create Flight"]
     [:form.flex.flex-col {:method :post :action "/flight"}
      (anti-forgery-field)
      [:label "From (ICAO)"]
      [:input.border.rounded.px-2.py-1 {:type :text :name :from}]
      [:label "To (ICAO)"]
      [:input.border.rounded.px-2.py-1 {:type :text :name :to}]
      [:label "Aircraft"]
      [:input.border.rounded.px-2.py-1 {:type :text :name :aircraft}]
      [:input.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg {:type :submit :value "Create Flight"}]]]
    [:div.mt-4.pr-16.mx-auto {:class "w-1/2"}
     [:h2.text-4xl.font-bold "Your flights"]
     [:div.flex.flex-col.justify-around (map flight-box flights)]]]))

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
     [:input.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg {:type :submit :value "Register"}]]
    [:a.w-full {:href "/login"}
     [:buton.mt-2.bg-blue-300.hover:bg-blue-400.border.rounded-lg "Login"]]]))