(ns clj-airbnb.init
  (:require [clojure.core.async
              :as async
              :refer [>! <! >!! <!! go go-loop chan buffer close! thread
                      alts! alts!!]]
             [clj-airbnb.core     :as core]
             [clj-airbnb.web      :as web]
             [clj-airbnb.alert    :as alert]
             [clj-airbnb.schedule :as sched])
  (:gen-class))

 ; "Load all alerts from db into queue for first time."
 ; def'ing only to make monitoring easier

(defn start 
  "Start all processes" 
  []
  (println "Starting all processes")
  (-> core/alert-queue ; returns atom of alerts to monitor
      (sched/start-scheduling) ; returns out channel of id's to update
      (core/listen-updates))  ; updates listings and processes changes
  (web/start-server))    ; start web server

(defn -main []
  (start))
