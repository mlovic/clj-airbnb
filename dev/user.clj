(ns user
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :refer (pprint)]
            [clojure.repl :refer :all]
            [clojure.test :as test]
            [clojure.tools.namespace.repl :refer (refresh refresh-all)]
            [clojure.core.async :as async]
            ))

 ;(:require [clojure.core.async
             ;:as async
             ;:refer [>! <! >!! <!! go go-loop chan buffer close! thread
                     ;alts! alts!! timeout]]))

;(defn echo-channel [c]
  ;(async/go-loop [out_num 1] 
                 ;(println "changes_out:" out_num ": " (<! c))
                 ;(recur (inc out_num))))