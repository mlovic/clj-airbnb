(ns clj-airbnb.core
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go go-loop chan buffer close! thread
                     alts! alts!!]])
  (:require [clj-airbnb.calendar :as cal])
  (:require [clj-airbnb.change :refer :all])
  (:require [clj-airbnb.airbnb :as airbnb])
  (:require [clj-airbnb.util :refer :all])
  (:require [clj-airbnb.listing :as li])
  (:gen-class))

(defn update-listing "doc-string" 
  [id, c] ; TODO optional channel
  (let [old     (:calendar (li/get id))
        nu      (airbnb/request-calendar id)
        changes (->> (cal/find-changes old nu)
                     (map #(assoc % :id id))
                     (map #(assoc % :change_seen (java.util.Date.))))]
    ;; make it return channel?
    (println "number of changes: " (count changes))
    (if (> (count changes) 0)
      (do 
        (go (doseq [change changes] 
              (>! c change)))
        (li/update-calendar id nu))
      (println "No changes for " id))))

(defn listen-updates 
  [c]
  (let [changes (chan)]
    (go-loop [] (update-listing (<! c) changes) (Thread/sleep 2000) (recur))
    changes)) ; doesn't go-loop  already return channel?

(defn update-all 
  "update all listings in the database" 
  []
  (let [update-queue (chan)]
    (-> update-queue (listen-updates) (listen-changes))
    (go
      (doseq [listing (li/get-all)]
        (>!! update-queue (:id listing))))))

;; TODO deal with fully qualified and ctags
(defn add-listing 
  "Add listing to database (with info and calendar) if not already present."
  [id]
  (if (li/get id)
    (println "Listing is already in database")
    (let [info    (airbnb/request-listing-info id) ; wanted fields defd in airbnb ns
          calendar (airbnb/request-calendar id)]
      (li/insert (merge info {:id id :calendar calendar :last_updated (java.util.Date.)} )))))

(load-listings)
