(ns clj-airbnb.fixture
  (:require [clj-airbnb.calendar :as cal]
            [clj-airbnb.airbnb :as airbnb]))

(defn fixture-path 
  "returns relative path for fixture" 
  [filename]
   ( str "fixtures/" filename))

(defn load-fixture  
  "loads fixture from file in fixtures directory"
  [filename]
  (let [path (fixture-path filename)] ;; set project root
    (read-string (slurp path))))
  
(defn create-fixture 
  "creates new fixture file in fixtures directory" 
  [data filename]
  (spit (fixture-path filename) (with-out-str (pr data)))
  )

(def cal (load-fixture "calendar.edn"))
(def cal-changed (update-in ( vec cal) [0 :available] not))

(comment
  (create-fixture (take 30 (airbnb/request-calendar 3255815)) "calendar.edn")
  )
