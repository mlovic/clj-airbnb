(ns clj-airbnb.notify
  (:require [postal.core :as postal]))
             
(defn notify! 
  "Notify about change"
  [change]
  (let [id (:id change)
        msg (str "There has been a change: " change)]
    (postal/send-message {:host "localhost"}
                         {:from "airbnb@mlovic.com"
                          :to "markolovic33@gmail.com"
                          :subject (str "Change for " id)
                          :body msg
                          :X-Tra "Something else"}))
  )
