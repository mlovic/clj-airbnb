(ns clj-airbnb.schedule
  (:require [clj-time.core :as t]
            [clojure.core.async :refer [offer! >!! >! go chan]]
            [clojure.tools.logging :as log]))

;; TODO do i need alert as dependency? maybe just pass in. Who should have
;; knowledge of alert->entry?

;; [:id [:freq :next-time]] next-time is nil when alert is on queue
(defn sched-entry 
  "Converts an alert to an alert schedule entry (size 2 vector of key and val)." 
  [alert]
  (let [entry-key (Integer. (:id alert))
        entry-val (assoc (dissoc alert :id) :next-time (t/now))]
    [entry-key entry-val]))

;; TODO try to keep sched sorted according to next time
(defn gen-update-schedule
  "Generate schedule from list of alerts"
  [alerts]
  (into (sorted-map) (map sched-entry alerts)))

(defn add-alert-to-queue 
  "Add an alert to the atom" 
  [sched alert]
  (swap! sched conj (sched-entry alert)))
;; TODO can't make integer a string

(defn update-next-time 
  "Update next-time of single entry in schedule"
  [entry]
  (log/debug "updating time for map: " entry)
  (assoc entry :next-time (-> (:freq entry) (t/minutes) (t/from-now))))

(defn call-every-minute [callback] 
  (future (while true (do (Thread/sleep (* 1000 60)) (callback)))))

(defn fire-scheduled 
  "iterates through alert queue and puts due alerts on update chan" 
  [queue out-chan]
  (log/debug "Checking alerts...")
  (doseq [[id alert] @queue]
    ;; TODO could have method here for a schedentry object
    (when (t/after? (t/now) (:next-time alert))
      ;; TODO maybe just call update funcgtion instead of using channels.
      ;; Could then get feedback about it before updating next-time.
      (log/debug "Alert due! " alert ". Putting on channel..." 
                 "(" (count (.buf out-chan)) " listings on channel)")
      (if-not (offer! out-chan id)
        (log/error "Update channel is full!"))
      ; make :next-time nil until listen-updates updates it
      (swap! queue update-in [id] #(assoc % :next-time nil)) ;need the explcit fn?
      )))

(defn start-scheduling 
  "Starts monitoring the alert queue and putting id's for listngs on the 
  channel that it returns." 
  ([alert-queue]
   ;; Using 
     (start-scheduling alert-queue (chan 500)))
  ([alert-queue update-queue]
     (call-every-minute #(fire-scheduled alert-queue update-queue))
     update-queue))
