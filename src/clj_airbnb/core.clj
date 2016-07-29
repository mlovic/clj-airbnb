(ns clj-airbnb.core
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go go-loop chan buffer close! thread
                     alts! alts!!]]
            [clj-airbnb.airbnb   :as airbnb]
            [clj-airbnb.listing  :as li]
            [clj-airbnb.alert    :as alert]
            [clj-airbnb.datastore :as store]
            [clojure.stacktrace]
            [clj-airbnb.schedule :as sched]
            [clojure.tools.logging :as log]))

(defonce alert-queue (atom (sched/gen-update-schedule (store/get-all-alerts))))

;; TODO deal with fully qualified and ctags
(defn add-listing 
  "Add listing to database (with info and calendar) if not already present."
  [id]
  (log/info "Adding listing " id)
  (if (store/get-listing id)
    (log/info "Listing is already in database!")
    (let [info    (airbnb/request-listing-info id) ; wanted fields defd in airbnb ns
          calendar (airbnb/request-calendar id)]
      (store/persist (li/make-listing id calendar info)))))

(defn add-alert 
  "Highest (business) level fn. Add new alert to system" 
  [alert]
  (if (store/get-alert-by-listing-id (:id alert)); get id in destructuring?
    (log/error "Trying to add alert which already exists!")
    (do 
      (log/info "Adding new alert: " alert)
      ;; Add listing if necessary
      (when-not (store/get-listing (:id alert)) ; need if-let?
        (add-listing (:id alert)))
      (store/persist (alert/map->Alert alert)) ; TODO consider if alert already exists
      (sched/add-alert-to-queue alert-queue alert))))

(require 'clj-time.format)
(require '[clj-time.core :as t])
;; maybe do with infinite lazy seq of dates. iterator
(defn dates-between 
  "Returns a list of all dates between start (inclusive) and end (exclusive)." ;vec?
  [start end] 
  (loop [dates [] 
         current start]
    (if (= current end) 
      dates
      (recur (conj dates current) 
             (t/plus- current (t/days 1))))))

(defn get-dates
  "Builds the list of dates from check in/out params in airbnb search-query" 
  [{checkin-str "checkin" checkout-str "checkout"}] ; can omit query?
  (let [formatter (clj-time.format/formatter "MM/dd/YY")
        parse    (fn [string] (clj-time.format/parse-local-date formatter string))
        checkin  (parse checkin-str)
        checkout (parse checkout-str)]
    (dates-between checkin checkout)
    ;(-> '(checkin checkout)
         ;(map parse)
         ;(apply dates-between))
    ))

(defn- enq-listings 
  [listings channel]
  (doseq [listing listings]
    (>!! channel listing)))

(defn get-listings
  "Takes query and returns a channel, where it will putting each listing.
  Closes when finished"
  ([query num-listings]
   (let [out (chan 5)]
     (go 
       (get-listings query num-listings 0 out))
     out))
  ([query num-listings offset out]
   (let [batch-size 50 ; move constant to airbnb ns?
         enq (fn [listings] (log/debug "Putting listings " (map :id listings) " on channel" ) (doseq [listing listings] (>!! out listing)))]
     (if (< num-listings batch-size) 
       (do
         (enq (airbnb/search-listings query num-listings offset))
         (close! out))
       (do
         (enq (airbnb/search-listings query batch-size offset)) 
         (get-listings query (- num-listings batch-size) (+ offset batch-size) out))))))

(comment
  (defn get-listings
    "Takes query and returns a channel, where it will putting each listing.
    Closes when finished"
    [query max-listings]
    (let [out (chan 5)
          batch-size 50] ; move constant to airbnb ns?
      (go
        (loop [offset 0])
        (doseq [offset (range 0 max-listings batch-size)]
          (doseq [listing (airbnb/search-listings query batch-size offset)]
            (log/debug "Putting listing " (:id listing) " on channel" )
            (>! out listing))))
      out)))

;; QUERY-ALERT       [freq dates ids params/query]
;; (Listing-?)Alert  [freq dates id]

;; TODO
;; take aq stuff out of sched
;; rethink Persistable protocol and where to put record defs
;; how going to store query?
;; make the long work async, and return fast on first success
(defn add-search-alert
  "Add search query alert to system"
  [location raw-query] ;; TODO figure this out
  (let [freq 120 ; TODO 
        dates (or nil (get-dates raw-query)) ;; TODO dates
        query (assoc raw-query :location location)
        max-listings 3] ; TODO 
    (log/info "Adding search alert with freq dates query:" freq dates query)
    (let [listings-chan (get-listings query max-listings)]
      (->
        (loop [ids []] 
          (if-let [li (<!! listings-chan)] ;put this directly in (go)-loop? or recur
            (do
              (log/info "Taking listing " (:id li) " off channel" )
              ;; have info, only need cal. But info from here and /listings is different
              ;; If listing already exists, update it without notification
              (add-listing (:id li))
              ;; TODO try to remove dependency on sched. Maybe w/protocol.
              (sched/add-alert-to-queue alert-queue {:freq freq 
                                                     :id (:id li)
                                                     :dates (set dates)})
              (recur (conj ids (:id li))))
            ids))
        ;; fix this mess. Or make async and reorder
        (#(store/persist (alert/->SearchAlert freq dates % query)))))))

#_(defn notify? [{:keys [listing-id date]} change]
    (some #{date} (:dates (listing-id alert-queue)))) ; change name to alerts?
