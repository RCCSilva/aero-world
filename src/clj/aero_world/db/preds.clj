(ns aero-world.db.preds
  (:require [clojure.string :as s]))

(defn icao? [icao]
  (and (= icao (s/upper-case icao)) (= (count icao) 4)))