(def server (-dev-main))

(use 'aero-world.db.core)

(add-aircraft! {:register "PT-RME" :type "C172" :airport "SBGR" :status :aircraft.status/available})

(transact! [{:order/from {:airport/icao "SBGR"}
             :order/to {:airport/icao "SBPA"}
             :order/status :order.status/available
             :order/product :order.product/apple
             :order/value 10.0}])

(transact! [{:order/from {:airport/icao "SBPA"}
             :order/to {:airport/icao "SBGR"}
             :order/status :order.status/available
             :order/product :order.product/banana
             :order/value 10.0}])

