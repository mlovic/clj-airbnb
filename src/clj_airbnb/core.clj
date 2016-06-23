(ns clj-airbnb.core
  (:require [clojure.core.async
              :as async
              :refer [>! <! >!! <!! go go-loop chan buffer close! thread
                      alts! alts!!]]
             [clj-airbnb.util                 :refer :all]
             [clj-airbnb.calendar :as cal]
             [clj-airbnb.change   :as ch]
             [clj-airbnb.airbnb   :as airbnb]
             [clj-airbnb.listing  :as li]
             [clj-airbnb.web      :as web]
             [clj-airbnb.alert    :as alert]
             [clojure.stacktrace]
             [clj-airbnb.schedule :as sched])
  (:gen-class))

#_(Thread/setDefaultUncaughtExceptionHandler
  (reify
    Thread$UncaughtExceptionHandler
    (uncaughtException [this thread throwable]
      (errorf throwable "Uncaught exception %s on thread %s" throwable thread))))

(defn log-buffer [label in]
  (go
    (let [item (<! in)]
      (item)
      )))

(comment
  (doseq [l ( li/query {:last_updated nil})]
    (do 
      (update-listing (:_id l) change-chan)
      (Thread/sleep 1000)))
   
  (defn update-oldest 
    "Updates the n listings with the oldest last_updated time" 
    [n]
    (li/get-oldest 3)
    ))

(defn update-listing "doc-string" 
  [id, c] ; TODO optional channel
  (println "updating listing " id)
  (let [old     (:calendar (li/get! id))
        nu      (airbnb/request-calendar id)
        ;new-days (difference (map :date old) (map :date nu));(find-new-days old nu)
        changes (->> (cal/find-changes old nu)
                     (map #(assoc % :id id))
                     (map #(assoc % :change_seen (java.util.Date.))))]
    (println "number of changes: " (count changes))
    (if (> (count changes) 0)
      (do 
        (go (doseq [change changes] 
              #_(>! c change) ;TODO get rid of channels
              (println "persisting change: " change)
              (ch/persist change)))
        (li/update-calendar id nu))
      (do
        (if (cal/new-days? old nu)
          (li/update-calendar id nu)
          (li/touch id))
        (println "No changes for " id)))))

(defn listen-updates 
  [c]
  (let [changes (chan)]
    (go-loop [] (update-listing (<! c) changes) 
             (Thread/sleep 2000) 
             (recur))
    changes)) ; doesn't go-loop  already return channel?

(defn update-all 
  "update all listings in the database" 
  []
  (let [update-queue (chan)]
    (-> update-queue (listen-updates) (ch/listen-changes))
    (go
      (doseq [listing (li/get-all)]
        (>!! update-queue (:_id listing))))))

;; TODO deal with fully qualified and ctags
(defn add-listing 
  "Add listing to database (with info and calendar) if not already present."
  [id]
  (if (li/get id)
    (println "Listing is already in database")
    (let [info    (airbnb/request-listing-info id) ; wanted fields defd in airbnb ns
          calendar (airbnb/request-calendar id)]
      (li/insert (merge info {:_id id 
                              :calendar calendar 
                              :last_updated (java.util.Date.)})))))

(defonce alert-queue (atom sched/gen-update-schedule)) ; def'ing only to make monitoring easier
(defn start 
  "Start all processes" 
  []
  (println "Starting all processes")
  (-> alert-queue ; returns atom of alerts to monitor
      (sched/start-scheduling) ; returns out channel of id's to update
      (listen-updates))  ; updates listings and processes changes
  (web/start-server))    ; start web server

(defn -main []
  (start))
