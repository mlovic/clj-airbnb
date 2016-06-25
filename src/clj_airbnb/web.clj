(ns clj-airbnb.web
  (:require [compojure.core :refer :all]
            [clojure.string :refer [join]]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [compojure.route :as route]
            [ring.adapter.jetty :refer :all]
            [ring.middleware.params :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            ;[ring.middleware.logger :as logger]
            [ring.util.response :refer :all]
            [clj-airbnb.alert :as alert]
            [clj-airbnb.core :as core]
            [clj-airbnb.listing :as li]))

(defroutes app-routes
  (GET "/" [id]
       (println id)
       (str (boolean (alert/get-by-listing-id (Integer. id)))))

  (POST "/" [id]
        (core/add-alert  {:freq 60 :id id}))

  (GET "/dash" [] 
       (join "<br>" (map li/summarize (li/get-all))
             )))

(defn wrap-logging [handler]
  (fn [request]
    (clojure.pprint/pprint request)
    (println (str (:request-method request)) " "
             (str (:uri request))
             (str (:id (:params request))))
    (handler request)))

(def app
  (-> app-routes 
      (wrap-defaults site-defaults)
      (wrap-params)
      (wrap-logging)
      #_(logger/wrap-with-logger)))

#_(defonce server (run-jetty #'app {:port 8080 :join? false}))

(defn start-server []
  (run-jetty #'app {:port 8080}))
