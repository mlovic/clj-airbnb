(ns clj-airbnb.init
  (:require
    [clj-airbnb.core     :as core]
    [clj-airbnb.web      :as web]
    [clj-airbnb.schedule :as sched]
    [clojure.tools.logging :as log])
  (:gen-class))

 ; "Load all alerts from db into queue for first time."
 ; def'ing only to make monitoring easier

(defn start 
  "Start all processes" 
  []
  (log/info "Starting all processes")
  (-> core/alert-queue ; returns atom of alerts to monitor
      (sched/start-scheduling) ; returns out channel of id's to update
      (core/listen-updates))  ; updates listings and processes changes
  (web/start-server))    ; start web server

(defn -main []
  (start))
