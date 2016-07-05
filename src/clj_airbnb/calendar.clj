(ns clj-airbnb.calendar
  (:require clojure.set
            [clojure.tools.logging :as log]))

(defn available? [day] (:available day))

(defn date-map 
  "Converts coll of days to a single map with structure -  date: true/false" 
  [cal]
  (reduce #(assoc %1 (:date %2) (:available %2)) {} cal))

(defn find-by-date 
  "Retrieve day from calendar by date" 
  [cal date]
  (first (filter #(= (:date %) date) cal)))

(defn new-days? 
  "Return true if new calendar has days not present in old calendar" 
  [old nu]
  (not (empty? (clojure.set/difference (set (map :date nu)) 
                                       (set (map :date old))))))

(defn get-change 
  "Get availability change between two days" 
  [old nu]
  (cond (and (:available old) (not (:available nu)))  "booked"
        (and (:available nu)  (not (:available old))) "cancelled"))

(defn common-dates
  "Return seq of dates present in both calendars"
  [cal1 cal2]
  (clojure.set/intersection (set (map :date cal1))
                            (set (map :date cal2))))

(defn find-change "doc-string" [date old-cal new-cal]
  (get-change (find-by-date old-cal date) 
              (find-by-date new-cal date)))

(defn find-changes
  "return seq of changes between two calendars"
  [old-cal new-cal] ; should id be here?
  (log/debug "num old dates: " (count old-cal) " | num new dates: " (count new-cal))
  (log/debug "going to compare " (count (common-dates old-cal new-cal)) " dates")
  (->> 
    ;; TODO deal with new dates
    (for [date (common-dates old-cal new-cal)]
      {:date date :direction (find-change date old-cal new-cal)})
    ;( #(doto (sort-by :date %) clojure.pprint/pprint)) 
    (filter :direction)))

take
(defn percent-available
  [calendar]
  ;; TODO Maybe only calculate over next 3 months
  (let [total-days (count calendar)
        available-days (count (filter available? calendar))]
    (if (= total-days 0)
      (str "--")
      (->>
        (/ available-days total-days)
        (float )
        (* 100 )
        (format "%.2f%%" )))))
    
