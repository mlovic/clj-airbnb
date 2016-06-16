(ns clj-airbnb.change
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]])
  (:require [monger.core :as monger]
            [monger.collection :as monger.coll])
  (:import [com.mongodb MongoOptions ServerAddress])
)

;; define functions in let block to save db-conf code?
(let [conn (monger/connect)
      db   (monger/get-db conn "clj-airbnb")]

  (defn persist-change [change]
    (monger.coll/insert db "changes" change))

  (defn get-changes []
    (monger.coll/find-maps db "changes")))

(defn listen-changes [c]
  (async/go-loop [out_num 1] 
                 (when-let [msg (<! c)]
                   (println "persisting change: " msg)
                   ;; TODO add timestamp
                   (persist-change msg)
                   (recur (inc out_num)))))
