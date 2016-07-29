(ns clj-airbnb.init
  (:require
    [clj-airbnb.core     :as core]
    [clj-airbnb.web      :as web]
    [clj-airbnb.schedule :as sched]
    [clj-airbnb.update   :as update]
    [clojure.tools.logging :as log])
  (:gen-class))

 ; "Load all alerts from db into queue for first time."
 ; def'ing only to make monitoring easier

;; log any uncaught exception on any thread. Code from Stuart Sierra's blog.
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (log/error ex "Uncaught exception on" (.getName thread)))))

(defn start 
  "Start all processes" 
  []
  (log/info "Starting all processes")
  (-> core/alert-queue ; returns atom of alerts to monitor
      (sched/start-scheduling) ; returns out channel of id's to update
      (update/listen-updates (fn [id] (swap! core/alert-queue update-in [id] sched/update-next-time))))  ; updates listings and processes changes
  (web/start-server))    ; start web server

(defn -main []
  (start))
