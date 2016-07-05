(ns clj-airbnb.change
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]])
  (:require [monger.core :as monger]
            [monger.collection :as mc]
            [environ.core :refer [env]]
            [clojure.tools.logging :as log])

  (:import [com.mongodb MongoOptions ServerAddress]))
(env :db-name)
(let [conn (monger/connect)
      db   (monger/get-db conn (env :db-name))]

  (defn persist [change]
    (mc/insert db "changes" change))

  (defn get-all []
    (mc/find-maps db "changes"))
  
  (defn get-for-listing [id] 
    (mc/find-maps db "changes" {:id id})))

(defn listen-changes [c]
  (async/go-loop [out_num 1] 
                 (when-let [msg (<! c)]
                   (log/debug "persisting change: " msg)
                   ;; TODO add timestamp
                   (persist msg)
                   (recur (inc out_num)))))
