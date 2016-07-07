(ns clj-airbnb.listing
  (:require [clj-airbnb.calendar :as cal]))

(defrecord Listing [id calendar last_updated city price name min_nights property_type])

(defn make-listing 
  "Construct a listing record" 
  [id cal info]
  (map->Listing
    (merge info {:_id id 
                 :calendar cal})))

(defn format-time [inst]
  (when inst 
    (.format (java.text.SimpleDateFormat. "HH:mm dd/MM/yy") inst)))

(defn summarize
  [listing]
    (str 
      (:_id listing) ": " (count (:calendar listing)) " days"
      " (" (cal/percent-available (:calendar listing)) " avail.) - "
      (:freq (:alert listing)) " - "
      (format-time (:last_updated listing))))
