(ns clj-airbnb.airbnb
  (:require [cheshire.core :refer :all]
            [clj-http.client :as client]
            [clj-time.core :as t]
            [clojure.tools.logging :as log]))

;; TODO should probably use same channel for all logging
(defn log-request "doc-string" []
  (spit "requests.log" (str (java.util.Date.) "\n") :append true))

(defn- current-month 
  "Return an integer representing the current month." 
  []
  (t/month (t/now)))

(defn get-calendar [response]
  (reduce 
    (fn [accum, month] (into accum (:days month))) 
    []
    (:calendar_months response)))

(defn request-calendar 
  "HTTP request for calendar of given listing" 
  [id]
  (log/info "Sending request..." id)
  ;; TODO set starting month dynamically
  (try
    (->
      ;; TODO increment count. fight now it's 6 for convenince
      (str "https://www.airbnb.com/api/v2/calendar_months?key=d306zoyjsyarp7ifhu67rjxn52tv0t20&currency=EUR&locale=en&listing_id=" id "=&month=" (current-month) "&year=2016&count=6&_format=with_conditions"  )
      (client/get)
      (:body);; handle exception here
      (parse-string true)
      (get-calendar))
    (finally (log/debug "Received response")
             (log-request)))
  )

(defn get-info 
  "extract needed info from json" 
  [response]
  (select-keys (:listing response)
               [:city :price :name :min_nights :property_type]))

(defn request-listing-info
  "HTTP request for listing info" 
  [id]
  (log/info "Sending request...")
  (try
    (->
      (str "https://api.airbnb.com/v2/listings/" id "?client_id=3092nxybyb0otqw18e8nh5nty&_format=v1_legacy_for_p3")
      (client/get)
      (:body);; handle exception here
      (parse-string true)
      (get-info))
    (finally (println "Received response")
             (log-request))))

(defn search-listings 
  "HTTP request to search for listings" 
  ([location price-min price-max limit]
   (search-listings location price-min price-max limit 0))
  ([location price-min price-max limit offset]
   (log/info "Sending request...")
   (try
     (->
       "https://api.airbnb.com/v2/search_results?client_id=3092nxybyb0otqw18e8nh5nty"
       (doto log/info)
       (client/get  
         {:query-params {"locale" "en-US"
                         "currency" "EUR" 
                         "_format" "for_search_results_with_minimal_pricing"
                         "sort" 1
                         "guests" 3
                         "room_types[]" "Entire home/apt"
                         "location" location
                         "price_max" price-max
                         "price_min" price-min
                         "_limit" limit
                         "_offset" offset
                         }})
       (:body)
       (parse-string true))
     (finally (println "Received response")
              (log-request)))))
