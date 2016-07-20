(ns clj-airbnb.notify
  (:require [environ.core :refer [env]]
            [postal.core :as postal]))
             
(defn notify! 
  "Notify about change"
  [changes]
  (try
    (let [id (:id (first changes))
          msg (str "There have been changes: " changes)]
      (postal/send-message {:host (env :smtp-host)}
                           {:from (env :mail-from) 
                            :to   (env :mail-to) 
                            :subject (str "Change(s) for " id)
                            :body msg
                            :X-Tra "Something else"}))
    (catch Exception e
      (prn "Error sending email!  " e)))
  )
