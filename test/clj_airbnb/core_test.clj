(ns clj-airbnb.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [clj-airbnb.calendar :as cal]
            [clj-airbnb.core :refer :all]))

#_(deftest get-dates-test
  (testing "core/get-dates"
    (is (= (get-dates) 1))))
  
(deftest find-changes-test
  (let [cal1 [{:date "2016-01-01"
               :available true}
              {:date "2016-01-02"
               :available false}]
        cal2 [{:date "2016-01-01"
               :available false}
              {:date "2016-01-02"
               :available true}]]
    (is (= '({:date "2016-01-01", :direction "booked"}
             {:date "2016-01-02", :direction "cancelled"}) 
           (cal/find-changes cal1 cal2))))
  (let [cal1 [{:date "2016-01-01"
               :available true}
              {:date "2016-01-02"
               :available false}]
        cal2 [{:date "2016-01-01"
               :available true}
              {:date "2016-01-02"
               :available false}]]
    (is (= '() (cal/find-changes cal1 cal2))))
  (let [cal1 [{:date "2016-01-01"
               :available true}
              ]
        cal2 [{:date "2016-01-01"
               :available false}
              {:date "2016-01-02"
               :available false}]]
    (is (= 1 (count (cal/find-changes cal1 cal2))))))
