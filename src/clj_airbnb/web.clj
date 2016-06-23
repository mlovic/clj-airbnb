(ns clj-airbnb.web
  (:require [ring.adapter.jetty :refer :all]
            [ring.middleware.params :refer :all]
            [ring.util.response :refer :all]
            [clj-airbnb.alert :as alert]))

(defn handler [{{id "id"} :params method :request-method}]
  (println "id: " id)
  (cond
    (= method :get)
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (str (boolean (alert/get-by-listing-id id)))}
    (= method :post)
    ;(do)
    ;(add-listing id)
    (alert/persist {:freq 60 :id id} 
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body "ok"})))

(def app
  (-> handler wrap-params))

#_(defonce server (run-jetty #'app {:port 8080 :join? false}))

(defn start-server []
  (run-jetty #'app {:port 8080}))
