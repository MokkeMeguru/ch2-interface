(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [guestbook.db.core :as db]
            [bouncer.core :as b]
            [bouncer.validators :as v]
            [ring.util.response :refer [response status]]))

(defn validate-message [params]
  (first
   (b/validate
    params
    :name v/required
    :message [v/required
              [v/min-count 10]])))

(defn home-page [{:keys [flash]}]
  (layout/render
   "home.html"
   ))

(defn about-page []
  (layout/render "about.html"))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> {:errors errors} response (status 400))
    (do
      (db/save-message!
       (assoc params :timestamp (java.util.Date.)))
      (response {:status :ok}))))

(defroutes home-routes
  (GET "/" request (home-page request))
  (GET "/about" [] (about-page))
  (POST "/add-message" req (save-message! req))
  (POST "/message" request (save-message! request)))

