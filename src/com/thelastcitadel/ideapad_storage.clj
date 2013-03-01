(ns com.thelastcitadel.ideapad-storage
  (:require [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [cemerick.friend.credentials :as creds]
            [compojure.core :refer [GET ANY POST defroutes]]
            [compojure.handler :refer [site]]
            [clojure.data.codec.base64 :refer [encode]])
  (:import (java.util UUID)
           (java.security MessageDigest)))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "password")
                    :roles #{::authenticated}}})

(def storage (atom {}))

(defn sha256 [seed]
  (let [md (MessageDigest/getInstance "SHA-256")]
    (.update md (.getBytes seed "utf8"))
    (.digest md)))

(defn trim= [string]
  (if (.endsWith string "=")
    (recur (subs string 0 (dec (count string))))
    string))

(defn store [{:keys [body]}]
  (let [x (slurp body)
        id (trim= (.replaceAll (.replaceAll (String. (encode (sha256 x)) "utf8") "-" "_")
                               "/" "+"))]
    (swap! storage assoc id x)
    {:body id}))

(alter-var-root #'store friend/wrap-authorize #{::authenticated})

(defn retrieve [{{:keys [id]} :params :as m}]
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
