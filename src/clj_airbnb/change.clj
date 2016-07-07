(ns clj-airbnb.change)

;; TODO change id to listing-id
(defrecord Change [id date direction change_seen])
  
;; not used
#_(defn listen-changes [c]
  (async/go-loop [out_num 1] 
                 (when-let [msg (<! c)]
                   (log/debug "persisting change: " msg)
                   ;; TODO add timestamp
                   (persist msg)
                   (recur (inc out_num)))))
