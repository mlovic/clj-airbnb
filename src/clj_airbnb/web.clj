(ns clj-airbnb.web
  (:require [compojure.core :refer :all]
            [clojure.string :refer [join]]
            [compojure.handler :as handler]
            [compojure.response :as response]
            [compojure.route :as route]
            [ring.adapter.jetty :refer :all]
            [ring.middleware.params :refer :all]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer :all]
            [clj-airbnb.alert :as alert]
            [clj-airbnb.listing :as li]))

(defroutes app-routes
  (GET "/" [id]
       (println id)
       (str (boolean (alert/get-by-listing-id (Integer. id)))))

  (POST "/" [id]
       #_(alert/persist {:freq 60 :id id}) 
        "this should add a new alert")

  (GET "/dash" [] 
    (join "<br>" (map li/summarize (li/get-all))
       )))

(def app
  (wrap-defaults app-routes site-defaults))

#_(defonce server (run-jetty #'app {:port 8080 :join? false}))

(defn start-server []
  (run-jetty #'app {:port 8080}))
