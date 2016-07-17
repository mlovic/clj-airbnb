(ns clj-airbnb.web
  (:require [compojure.core :refer :all]
            [clojure.string :refer [join]]
            [ring.adapter.jetty :refer :all]
            [ring.middleware.params :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.util.response :refer :all]
            [clj-airbnb.alert :as alert]
            [clj-airbnb.core :as core]
            [clj-airbnb.datastore :as store]
            [clj-airbnb.listing :as li]
            [clojure.tools.logging :as log]))

(defroutes app-routes
  (GET "/" [id]
       (if id
         (do
           (log/debug "RESPONSE: " (str (boolean (store/get-alert-by-listing-id (Integer. id)))))
           (str (boolean (store/get-alert-by-listing-id (Integer. id)))))
         (redirect "/dash")))

  (POST "/" [id]
        ;; TODO do not add alert if already exists
        (core/add-alert  {:freq 60 :id (Integer. id)})
        "ok")

  (GET "/dash" [] 
       (join "<br>" (map li/summarize (store/get-all-listings))
             )))

(defn wrap-logging [handler]
  (fn [request]
    (clojure.pprint/pprint request)
    (log/info (str (:request-method request)) " "
              (str (:uri request))
              (str (:id (:params request))))
    (handler request)))

(def app
  (-> app-routes 
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))
      (wrap-params)
      #_(wrap-logging)
      (wrap-with-logger)))

#_(defonce server (run-jetty #'app {:port 8080 :join? false}))

(defn start-server []
  (run-jetty #'app {:port 8080}))
