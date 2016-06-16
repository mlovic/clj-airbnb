(ns clj-airbnb.store
  (:require [clj-airbnb.calendar :as cal])
  ;(:require [clojure.java.jdbc :refer :all])
  )

(def listings (atom []))

(defn get-listings [] @listings)

(defn get-listing 
  "Returns listing by id. Returns nil if not found."
  [id]
  (first 
    (filter #(= (:id %) id) 
            @listings)))

(defn get-listing! 
  "Returns listing by id. Throws exception if not found."
  [id] 
  (or
    (get-listing id)
    (throw (Exception. (str "Listing not found for id: " id)))))

;(swap! listings conj ((assoc ( :calendar ( get-listing 79800993)) :calendar)

(defn put-listing! [listing]
    (swap! listings conj listing))

(defn remove-listing! [id]
  ;; FIXME deletes everything
  (swap! listings (fn [coll] (remove #(= (:id %) id) coll))));; confusing

(defn persist-listings []
  (spit "listings.data" (with-out-str (pr @listings))))

(defn load-listings []
  (reset! listings ( read-string (slurp "listings.data"))))

;(def db
  ;{:classname   "org.sqlite.JDBC"
   ;:subprotocol "sqlite"
   ;:subname     "db/clj-airbnb.db"
   ;})

;(defn create-db []
  ;(try 
    ;(db-do-commands db
                    ;(create-table-ddl :days
                                      ;[ ;[:availability :boolean]
                                       ;[:listing-id :int]
                                       ;;[:date :date]
                                       ;]))
    ;(catch Exception e (println e))))
