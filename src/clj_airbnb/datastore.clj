(ns clj-airbnb.datastore
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer [$exists $set $bit]]
            [environ.core :refer [env]]
            [clj-airbnb.listing :as li]
            [clj-airbnb.change :as change]
            [clojure.tools.logging :as log]
            [clj-airbnb.alert :as alert]
            )
  (:import [clj_airbnb.listing Listing]
           [clj_airbnb.change  Change]
           [clj_airbnb.alert   Alert]
           [clj_airbnb.alert   SearchAlert]
           )
  (:import [com.mongodb MongoOptions ServerAddress])
  (:import org.bson.types.ObjectId))

(def db (mg/get-db (mg/connect) 
                   (env :db-name)))

(defprotocol Persistable
  (persist [obj]))

(extend-protocol Persistable
  Listing
  (persist [listing]
    (let [record (assoc listing :last_updated (java.util.Date.))]
      (log/debug "Persisting listing to db: " (li/summarize record))
      (mc/insert db "listing" record)))
  Change
  (persist [change]
    (log/debug "Persisting change to db: " change)
    (mc/insert db "changes" change))
  SearchAlert
  (persist [search-alert]
    (log/debug "Persisting search-alert to db: " search-alert)
    (mc/insert db "searchAlerts" search-alert))
  Alert
  (persist 
    [alert]
    (let [id (:id alert)
          alert-map (dissoc alert :id)]
      (mc/update-by-id db "listing" id {$set {:alert alert-map}} ))))
;; TODO need to change coll name "listing" to plural

;;; LISTINGS
(defn get-all-listings []
  (mc/find-maps db "listing"))

(defn get-listing [id]
  (mc/find-map-by-id db "listing" id))

;; could use option map for get
(defn get-listing! [id]
  (or (get-listing id)
      (throw (Exception. (str "Listing not found for id: " id)))))

(defn touch-listing 
    "Update :last_updated field for listing." 
    [id]
    (mc/update-by-id db "listing" id {$set {:last_updated (java.util.Date.)}}))

(defn update-calendar 
  "Replaces the calendar for a listing in the db." 
  [id cal]
  (log/debug "Updating calendar for " id)
  (mc/update-by-id db "listing" id {$set { :calendar cal :last_updated (java.util.Date.)}})
  (li/summarize (get-listing id))) ; TODO get rid of this line

;;; ALERTS
(defn get-alert-by-listing-id 
    "Get alert from db by its listing id. Return nil if either alert 
    or listing don't exist"
    [id]
    (when-let [listing (get-listing id)]
      (assoc (:alert listing) :id id)))

(defn get-all-alerts []
    (for [alert-map (mc/find-maps db "listing" {:alert {$exists true}} 
                                  ["alert" "_id"])]
      {:id (:_id alert-map) :freq (:freq (:alert alert-map))}))

;;; NOT USED
(defn get-changes-for-listing [id] 
  (mc/find-maps db "changes" {:id id})) ;; change id to listing-id

#_(defn flip-date
  "Flip the availability of date n in cal in db" 
  [id n]
  (let [day (nth (:calendar (mc/find-map-by-id db "listing" id)) n)
        date (:date day)
        currnt (cal/available? day)]
    (log/info (str "flipped date: " date " to " currnt))
    (mc/update-by-id db "listing" id
                     {$set { (str "calendar." n ".available") (not currnt)}})))

(defn summarize-all []
  (doseq [summary (map li/summarize (get-all-listings))]
    (println summary)))
