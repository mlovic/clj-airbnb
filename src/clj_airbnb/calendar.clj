(ns clj-airbnb.calendar
  (:require clojure.set))

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
  (println "num old dates: " (count old-cal) " | num new dates: " (count new-cal))
  (println "going to compare " (count (common-dates old-cal new-cal)) " dates")
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
    

;;; == OLD CODE ==========================

; Could instead make calendar have dates as keys

;(defn process-changes 
  ;"Finds changes between two cals and puts them on changes channel" 
  ;[id new-cal old-cal c]
  ;(let [dates (map :date new-cal)]
    ;;; Maybe use reduce instead to return a map
    ;(doseq [date dates] 
      ;(let [ change (find-change date old-cal new-cal)]
        ;(if change
          ;(do 
            ;(println "Putting change " change)
            ;( >!! c {:date date 
                     ;:direction change
                     ;:listing-id id 
                     ;:date_recorded (java.util.Date.)
                     ;}
                  ;)))))))

;(defn calendar-to-map
  ;[calendar]
  ;(reduce #(assoc %1 (:date %2) (:available %2))
          ;{}
          ;calendar))

;(defn cancellation? "doc-string" [old nu] ; false)
  ;(if (complement (available? old))
    ;(available? nu)
    ;(false)))

    ;(reduce 
      ;(fn [accum date]
        ;(let [old-available (get old date) 
              ;new-available (get nu date)]
          ;(assoc accum 
                 ;date 
                 ;(cond (and old-available (not new-available)) "booked"
                       ;(and new-available (not old-available)) "cancelled"
                       ;:else "no change"))))
      ;{}
      ;dates)))

;(defn check-changes "Ch" 
  ;[new-cal old-cal]
  ;(let [dates (map :date new-cal)
        ;nu (calendar-to-map new-cal)
        ;old (calendar-to-map old-cal)]
    ;(reduce 
      ;(fn [accum date]
        ;(let [old-available (get old date) 
              ;new-available (get nu date)]
          ;(assoc accum 
                 ;date 
                 ;(cond (and old-available (not new-available)) "booked"
                       ;(and new-available (not old-available)) "cancelled"
                       ;:else "no change"))))
      ;{}
      ;dates)))

    ;(map #(if ( cancellation? %2, %3) %1) dates, new-days, old-days)))

