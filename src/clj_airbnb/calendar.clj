(ns clj-airbnb.calendar)


(defn available? [day] (:available day))

(defn find-by-date 
  "Retrieve day from calendar by date" 
  [cal date]
  (first (filter #(= (:date %) date) cal)))

(defn get-change 
  "Get availability change between two days" 
  [old nu]
  (cond (and (:available old) (not (:available nu)))  "booked"
        (and (:available nu)  (not (:available old))) "cancelled"))

(defn find-change "doc-string" [date old-cal new-cal]
  (get-change (find-by-date old-cal date) 
              (find-by-date new-cal date)))

;; get-changes conflicts with db retrieve fn 
(defn find-changes
  "return seq of changes between two calendars"
  [new-cal old-cal] ; should id be here?
  (->> 
    (for [date (map :date new-cal)]
      {:date date :direction (find-change date old-cal new-cal)})
    (filter :direction)))

(defn percent-available
  [calendar]
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

