(ns clj-airbnb.alert
  (:require [monger.core :as monger]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [monger.operators :refer [$exists $set]]
            [clj-airbnb.listing :as li]
            [clj-time.periodic :refer [periodic-seq]])
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]])
  (:require [clj-airbnb.calendar :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress]))
 
;(defrecord Alert [listing-id freq])

;; TODO use same conn for al ns's
(let [conn (monger/connect)
      db   (monger/get-db conn "clj-airbnb")]

  (defn get-by-listing-id 
    "Get alert from db by its listing id. Return nil if either alert 
    or listing don't exist"
    [id]
    (assoc (:alert (li/get id) :id id)))

  (defn persist 
    [alert]
    (let [id (:id alert)
          alert-map (dissoc alert :id)]
      (mc/update-by-id db "listing" id {$set {:alert alert-map}} )))

  (defn get-all []
    (for [alert-map (mc/find-maps db "listing" {:alert {$exists true}} 
                                  ["alert" "_id"])]
      {:id (:_id alert-map) :freq (:freq (:alert alert-map))})))
      ;(->Alert (:id alert-map) (:freq (:alert alert-map))))))

 ;(periodic-seq (t/now)
               ;(-> 5 t/minutes))

#_(defn test-chimes []
  (let [chimes (chime-ch [(-> 2 t/seconds t/from-now)
                          (-> 3 t/seconds t/from-now)])]
    (go-loop []
             (when-let [msg (<! chimes)]
               (println "Chiming at:" msg)
               (recur)))))

#_(defn start-alert [c alert]
  (go-loop 
    []
    (alt!
      timeout)))

#_(defn start-monitor [c]
  (let [alerts (get-all)]
    (doseq [alert alerts]
      (start-alert c alert))))
