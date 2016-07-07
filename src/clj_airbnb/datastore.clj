(ns clj-airbnb.datastore
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.operators :refer [$set $bit]]
            [environ.core :refer [env]]
            [clj-airbnb.listing :as li]
            [clojure.tools.logging :as log]
            [clj-airbnb.alert :as alert])
  (:import [clj_airbnb.listing Listing]
           [clj_airbnb.change  Change])
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
    (mc/insert db "changes" change)))

(defn get-changes-for-listing [id] 
  (mc/find-maps db "changes" {:id id})) ;; change id to listing-id

