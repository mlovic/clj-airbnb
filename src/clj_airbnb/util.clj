(ns clj-airbnb.util
  (:require [clojure.core.async
             :as async
             :refer [>! <! >!! <!! go go-loop chan buffer close! thread
                     alts! alts!! timeout]]))

(defn echo-channel [c]
  (async/go-loop [out_num 1] 
                 (println "changes_out:" out_num ": " (<! c))
                 (recur (inc out_num))))

