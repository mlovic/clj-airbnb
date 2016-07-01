(ns clj-airbnb.notify
  (:require [postal.core :as postal]))
             
(defn notify! 
  "Notify about change"
  [changes]
  (let [id (:id (first changes))
        msg (str "There have been changes: " changes)]
    (postal/send-message {:host "localhost"}
                         {:from "airbnb@mlovic.com"
                          :to "markolovic33@gmail.com"
                          :subject (str "Change(s) for " id)
                          :body msg
                          :X-Tra "Something else"}))
  )
