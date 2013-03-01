(ns com.thelastcitadel.ideapad-storage
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [cemerick.friend.credentials :as creds]
            [compojure.core :refer [GET ANY POST defroutes]]
            [compojure.handler :refer [site]])
  (:import (java.util UUID)))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "password")
                    :roles #{::authenticated}}})

(def storage (atom {}))

(defn store [{:keys [body]}]
  (prn `store)
  (let [id (str (UUID/randomUUID))]
    (swap! storage assoc id (slurp body))
    {:body id}))

(alter-var-root #'store friend/wrap-authorize #{::authenticated})

(defn retrieve [{{:keys [id]} :params :as m}]
  (prn `retrieve)
  (prn m)
  (if (contains? @storage id)
    {:body (get @storage id)}
    {:status 404
     :body ""}))

(alter-var-root #'retrieve friend/wrap-authorize #{::authenticated})

(defroutes app
  (ANY "/" request {:body "Nothing to see here"})
  (ANY "/login" request {:body "Yo"})
  (ANY "/store" request store)
  (ANY "/retrieve" request retrieve)
  (friend/logout
   (ANY "/logout" request (ring.util.response/redirect "/"))))

(def handler
  (-> #'app
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn users)
        :workflows [(workflows/interactive-form)]})
      site))
