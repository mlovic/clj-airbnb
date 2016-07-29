 (ns clj-airbnb.update
  (:require [clojure.core.async    :refer [>! <! >!! <!! go go-loop chan ]]
            [clj-airbnb.airbnb     :as airbnb]
            [clj-airbnb.listing    :as li]
            [clj-airbnb.datastore  :as store]
            [clojure.tools.logging :as log]
            [clj-airbnb.calendar   :as cal]
            [clj-airbnb.change     :as change]
            [clj-airbnb.notify     :as notify]))           
;; TODO Does too much: get cal from db, request cal, find changes, build changes, send notification, persist changes, persist new cal, 
(defn update-listing "doc-string" 
  [id, c] ; TODO optional channel
  (log/info "Updating listing " id)
  (let [old     (:calendar (store/get-listing! id))
        nu      (airbnb/request-calendar id)
        ;new-days (difference (map :date old) (map :date nu));(find-new-days old nu)
        ;; TODO push more of functionality below to change ns
        changes (->> (cal/find-changes old nu)
                     ;; implement as comp'd transducers?
                     (map #(assoc % :id id))
                     (map #(assoc % :change_seen (java.util.Date.))))]
    (log/info "Found " (count changes) " changes for " id )
    (if (> (count changes) 0)
      (do 
        (log/info "Sending email for changes in " id)
        (notify/notify! (into () (doall changes))) ; convert lazyseq to list 
        (log/debug "persisting changes...")
        (go (doseq [change changes] 
              #_(>! c change) ;TODO get rid of channels
              (store/persist (change/map->Change change))))
        (store/update-calendar id nu)
        )
      (do
        (if (cal/new-days? old nu)
          (store/update-calendar id nu)
          (store/touch-listing id))
        (log/debug "No changes for " id)))))

(defn listen-updates 
  [c callback]
  (let [changes (chan)
        updated (chan)]
    (go-loop [] 
             (let [id (<! c)]
               (try (update-listing id changes)
                 (catch Exception e
                   (log/error e "Error updating listing " id)))
               (callback id)
               )
             (Thread/sleep 2000) 
             (recur))
    changes))
