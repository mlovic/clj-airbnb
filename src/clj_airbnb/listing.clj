(ns clj-airbnb.listing
  (:require [monger.core :as monger]
            [monger.collection :as mc]
            [clj-airbnb.calendar :as cal])
  (:import [com.mongodb MongoOptions ServerAddress])
  (:import org.bson.types.ObjectId))


  (let [conn (monger/connect)
        db   (monger/get-db conn "clj-airbnb")
        table "listing"]

    (defn insert [record]
      (mc/insert db table record))

    (defn get-all []
      (mc/find-maps db table))

    ;; TODO need alternative exception-throwing method?
    (defn get [id]
      (mc/find-by-id db table id))

    (defn get! [id]
      (or (get id)
          (throw (Exception. (str "Listing not found for id: " id))))))

;; need to change to plural

(def db (monger/get-db (monger/connect) "clj-airbnb"))
(def table "listing")

(defn format-time [inst]
  (if inst 
    (.format (java.text.SimpleDateFormat. "HH:mm dd/MM/yy") inst)
    nil))

(defn summarize
  [listing]
  (println 
    (str 
      (:id listing) ": " (count (:calendar listing)) " days"
      " (" (cal/percent-available (:calendar listing)) " avail.) - "
      (format-time (:last_updated listing))
      )))

(defn summarize-all []
  (map summarize (get-all)))

(defn update-calendar 
  "replaces the calendar for a listing in the db" 
  [id cal]
  ;; TODO look at other monger projects
  (println "Updating calendar for " id)
  (mc/update-by-id db table id {:calendar cal :last_updated (java.util.Date.)}))

(comment
  (let [ls ( get-all)
        db (monger/get-db (monger/connect) "clj-airbnb")]
    (doseq [l ls]
      (println 
        (str "changing " (:_id l) " to " (:id l)))
      (mc/insert db "listing" (assoc l :_id (:id l)) )))

  (def db (monger/get-db (monger/connect) "clj-airbnb"))
  (map :_id (mc/find-maps db "listing" {:id 8725004}))
  (mc/remove db "listing" {:id 8725004})

  (doseq [oid (remove integer? (map :_id ( get-all)))]
    (mc/remove-by-id db "listing" oid))
  (count (map :id ( get-all))))
 ;(doseq [l (get-all)] (clj-airbnb.store/summarize-listing l))

