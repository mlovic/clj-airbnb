(ns clj-airbnb.alert)
 

;; TODO use same conn for al ns's

(defrecord Alert [id freq])
(defrecord SearchAlert [freq dates ids query])
