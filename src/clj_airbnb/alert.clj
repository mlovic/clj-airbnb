(ns clj-airbnb.alert
  (:require [monger.core :as monger]
            [monger.collection :as mc]
            [monger.operators :refer [$exists $set]]
            [clj-airbnb.listing :as li]
            [clj-time.core :as t])
  (:import [com.mongodb MongoOptions ServerAddress]))
 
;(defrecord Alert [listing-id freq])

;; TODO use same conn for al ns's
(let [conn (monger/connect)
      db   (monger/get-db conn "clj-airbnb")]

  (defn get-by-listing-id 
    "Get alert from db by its listing id. Return nil if either alert 
    or listing don't exist"
    [id]
    (when-let [listing (li/get id)]
      (assoc (:alert listing) :id id)))

  (defn persist 
    [alert]
    (let [id (:id alert)
          alert-map (dissoc alert :id)]
      (mc/update-by-id db "listing" id {$set {:alert alert-map}} )))

  (defn get-all []
    (for [alert-map (mc/find-maps db "listing" {:alert {$exists true}} 
                                  ["alert" "_id"])]
      {:id (:_id alert-map) :freq (:freq (:alert alert-map))}))
  
  (defn add 
    "Highest (business) level fn. Add new alert to system" 
    [alert, sched]
    ;(exist)
    ))
      ;(->Alert (:id alert-map) (:freq (:alert alert-map))))))
