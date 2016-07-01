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
            [clj-airbnb.alert    :as alert]
            [clojure.stacktrace]
            [clj-airbnb.schedule :as sched]
            [clojure.tools.logging :as log]
            [clj-airbnb.notify :as notify]))

#_(Thread/setDefaultUncaughtExceptionHandler
  (reify
    Thread$UncaughtExceptionHandler
    (uncaughtException [this thread throwable]
      (errorf throwable "Uncaught exception %s on thread %s" throwable thread))))

(defonce alert-queue (atom (sched/gen-update-schedule (alert/get-all))))

(defn update-listing "doc-string" 
  [id, c] ; TODO optional channel
  (log/info "Updating listing " id)
  (let [old     (:calendar (li/get! id))
        nu      (airbnb/request-calendar id)
        ;new-days (difference (map :date old) (map :date nu));(find-new-days old nu)
        ;; TODO push more of functionality below to change ns
        changes (->> (cal/find-changes old nu)
                     (map #(assoc % :id id))
                     (map #(assoc % :change_seen (java.util.Date.))))]
    (log/info "Found " (count changes) " changes for " id )
    (if (> (count changes) 0)
      (do 
        (notify/notify! changes) ;; TODO careful with this!!
        (go (doseq [change changes] 
              #_(>! c change) ;TODO get rid of channels
              (log/debug "persisting change: " change)
              (ch/persist change)))
        (li/update-calendar id nu))
      (do
        (if (cal/new-days? old nu)
          (li/update-calendar id nu)
          (li/touch id))
        (log/debug "No changes for " id)))))

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
  (log/info "Adding listing " id)
  (if (li/get id)
    (log/info "Listing is already in database!")
    (let [info    (airbnb/request-listing-info id) ; wanted fields defd in airbnb ns
          calendar (airbnb/request-calendar id)]
      (li/insert (merge info {:_id id 
                              :calendar calendar 
                              :last_updated (java.util.Date.)})))))

(type (key ( first @alert-queue)))
(defn add-alert 
  "Highest (business) level fn. Add new alert to system" 
  [alert]
  (if (alert/get-by-listing-id (:id alert)); get id in destructuring?
    (log/error "Trying to add alert which already exists!")
    (do 
      (log/info "Adding new alert: " alert)
      ;; Add listing if necessary
      (when-not (li/get (:id alert)) ; need if-let?
        (add-listing (:id alert)))
      (alert/persist alert) ; TODO consider if alert already exists
      (sched/add-alert-to-queue alert-queue alert))))

