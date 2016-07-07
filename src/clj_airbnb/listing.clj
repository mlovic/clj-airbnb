(ns clj-airbnb.listing
  (:require [monger.core :as monger]
            [monger.collection :as mc]
            [monger.operators :refer [$set $bit]]
            [environ.core :refer [env]]
            [clj-airbnb.calendar :as cal]
            [clojure.tools.logging :as log])
  (:import [com.mongodb MongoOptions ServerAddress])
  (:import org.bson.types.ObjectId))

(declare summarize)

(defrecord Listing [id calendar last_updated city price name min_nights property_type])

(defn make-listing 
  "Construct a listing record" 
  [id cal info]
  (map->Listing
    (merge info {:_id id 
                 :calendar cal})))

;; TODO need to change coll name "listing" to plural
(let [conn (monger/connect)
      db   (monger/get-db conn (env :db-name))
      table "listing"]
  
  (defn query 
    "use monger.coll/find-maps to query db for given map" 
    [query]
    (mc/find-maps db table query))

  (defn get-all []
    (mc/find-maps db table))

  (defn get [id]
    (mc/find-map-by-id db table id))

  ;; could use option map for get
  (defn get! [id]
    (or (get id)
        (throw (Exception. (str "Listing not found for id: " id)))))
  
  (defn touch 
    "update :last_updated field for listing" 
    [id]
    (mc/update-by-id db table id {$set {:last_updated (java.util.Date.)}}))

  (defn format-time [inst]
    (when inst 
      (.format (java.text.SimpleDateFormat. "HH:mm dd/MM/yy") inst)))

  (defn update-calendar 
    "replaces the calendar for a listing in the db" 
    [id cal]
    (log/debug "Updating calendar for " id)
    (mc/update-by-id db table id {$set { :calendar cal :last_updated (java.util.Date.)}})
    (summarize (get id)))

  (defn flip-date
    "Flip the availability of date n in cal in db" 
    [id n]
    (let [day (nth (:calendar (mc/find-map-by-id db table id)) n)
          date (:date day)
          currnt (cal/available? day)]
      (log/info (str "flipped date: " date " to " currnt))
      (mc/update-by-id db table id
                     {$set { (str "calendar." n ".available") (not currnt)}}))))

(defn summarize
  [listing]
    (str 
      (:_id listing) ": " (count (:calendar listing)) " days"
      " (" (cal/percent-available (:calendar listing)) " avail.) - "
      (:freq (:alert listing)) " - "
      (format-time (:last_updated listing))))

(defn summarize-all []
  (doseq [summary (map summarize (get-all))]
    (println summary)))

(comment

  (def db (monger/get-db (monger/connect) "clj-airbnb"))
  (def table "listing")
  (summarize-all)
  (nth (:calendar (mc/find-map-by-id db table 9850900)) 0)

  (let [ls (get-all)] 
    (doseq [l ls]
      (println 
        (str "changing " (:_id l) " to " (:id l)))
      (mc/insert db "listing" (assoc l :_id (:id l)) )))

  (def db (monger/get-db (monger/connect) "clj-airbnb"))
  (map :_id (mc/find-maps db "listing" {:id 8725004}))
  (mc/remove db "listing" {:id 8725004})

  (remove integer? (map :_id ( get-all)))

  (doseq [oid (remove integer? (map :_id ( get-all)))]
    (mc/remove-by-id db "listing" oid))
  (count (map :id ( get-all))))

