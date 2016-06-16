(ns clj-airbnb.alert
  (:require [monger.core :as monger]
            [monger.collection :as monger.coll]
            [chime :refer [chime-ch]]
            [clj-time.core :as t]
            [clj-time.periodic :refer [periodic-seq]])
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout go-loop]])
  (:require [clj-airbnb.calendar :refer :all])
  (:import [com.mongodb MongoOptions ServerAddress]))
 
(defrecord Alert [listing-id freq])

(let [conn (monger/connect)
      db   (monger/get-db conn "clj-airbnb")]

  (defn persist-alert [alert]
    (monger.coll/insert db "alerts" alert))

  (defn get-alerts []
    (map map->Alert (monger.coll/find-maps db "alerts"))))

 ;(periodic-seq (t/now)
               ;(-> 5 t/minutes))


(defn test-chimes []
  (let [chimes (chime-ch [(-> 2 t/seconds t/from-now)
                          (-> 3 t/seconds t/from-now)])]
    (go-loop []
             (when-let [msg (<! chimes)]
               (println "Chiming at:" msg)
               (recur)))))

(defn start-alert [c alert]
  (go-loop 
    []
    (alt!
      timeout))
  )
(defn start-monitor [c]
  (let [alerts get-alerts]
    (doseq [alert alerts]
      (start-alert c alert)))
  )
