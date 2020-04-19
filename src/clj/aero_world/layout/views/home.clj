(ns aero-world.layout.views.home)

(def navbar 
  [:nav.px-16.py-2.bg-white.shadow-lg
   [:div.flex.flex-row.items-center.justify-between
    [:div.flex.flex-row.items-center
     [:a {:href "/"}
      [:span.text-2xl.font-bold.text-gray-800 "Aero World"]]]
    [:div
     [:span.pr-8.font-bold.text-gray-800]
     [:a {:href "/login"}
      [:button.w-32.mr-2.border-2.border-blue-500.hover:border-blue-600.hover:bg-blue-600.rounded.px-4.py-2.font-bold.text-blue-500.hover:text-white
       "Login"]]
     [:a {:href "/register"}
      [:button.w-32.border-2.border-blue-500.hover:border-blue-600.bg-blue-500.hover:bg-blue-600.rounded.px-4.py-2.font-bold.text-white  
       "Register"]]]]])

(defn home [params]
  [:div
   [:div navbar]
   [:div.flex.flex-col
    [:div.flex.px-16.mt-32
     [:h2.font-bold.text-gray-900.text-4xl "Making Flight Simulators more real!"]]]])