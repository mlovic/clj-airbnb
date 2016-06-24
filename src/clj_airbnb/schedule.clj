(ns clj-airbnb.schedule
  (:require [clj-time.core :as t]
            [clj-airbnb.alert :as alert]
            [clojure.core.async :refer [>! go chan]]))

;; TODO do i need alert as dependency? maybe just pass in. Who should have
;; knowledge of alert->entry?

(defn sched-entry 
  "Converts an alert to an alert schedule entry (size 2 vector of key and val)." 
  [alert]
  (let [entry-key (:id alert)
        entry-val (assoc (dissoc alert :id) :next-time (t/now))]
    [entry-key entry-val]))

;; TODO try to keep sched sorted according to next time
(defn gen-update-schedule
  "Load all alerts from db into queue for first time."
  []
  (into (sorted-map) (map sched-entry (alert/get-all))))

(defn add-alert-to-queue 
  "Add an alert to the atom" 
  [sched alert]
  (swap! sched conj (sched-entry alert)))

(defn update-next-time 
  "Update next-time of single entry in schedule"
  [entry]
  (println "updating time for map: " entry)
  (assoc entry :next-time (-> (:freq entry) (t/minutes) (t/from-now))))

(defn call-every-minute [callback] 
  (future (while true (do (Thread/sleep (* 1000 60)) (callback)))))

(defn fire-scheduled 
  "iterates through alert queue and puts due alerts on update chan" 
  [queue out-chan]
  (println "Checking alerts...")
  (doseq [[id alert] @queue]
    (when (t/after? (t/now) (:next-time alert))
      (println "alert due!  " alert)
      (go 
        (println "putting id " id " on channel...")
        (>! out-chan id)
        (swap! queue update-in [id] update-next-time)))
          ))

(defn start-scheduling 
  "Starts monitoring the alert queue and putting id's for listngs on the 
  channel that it returns." 
  ([alert-queue]
     (start-scheduling alert-queue (chan)))
  ([alert-queue update-queue]
     (call-every-minute #(fire-scheduled alert-queue update-queue))
     update-queue))

;;(reduce #(conj %1 {(:id %2) (dissoc %2 :id)}) {} (alert/gen-alert-queue))))

