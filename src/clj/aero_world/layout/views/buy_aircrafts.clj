(ns aero-world.layout.views.buy-aircrafts
  (:require [aero-world.layout.components :refer [navbar]]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn build-aircraft-model-for-option [aircraft-model]
  [:option {:value (-> aircraft-model  :aircraft-model/type)} (format "%s ($%s)" 
                                                                      ( -> aircraft-model :aircraft-model/type)
                                                                      (-> aircraft-model :aircraft-model/price))])

(defn buy-aircrafts [{:keys [current-user aircraft-models]}]
  [:div
   [:div (navbar (-> current-user :user/username))]
   [:div.mt-4.px-16
    [:div.mx-auto {:class "w-1/2"}
     [:h2.px-auto.text-2xl.text-gray-800.font-bold "Available Aircrafts to Buy"]
     [:form.flex.flex-col {:method :post :action "/buy-aircrafts"}
      (anti-forgery-field)
      [:select.border.rounded.px-2.py-1 {:name :type}
       (map build-aircraft-model-for-option aircraft-models)]
      [:label "Register"]
      [:input.border.rounded.px-2.py-1 {:type :text :name :register}]
      [:input.py-2.mt-2.bg-blue-500.hover:bg-blue-600.border.rounded-lg.cursor-pointer.font-bold.text-white
       {:type :submit :value "Buy"}]]]]])