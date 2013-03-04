(ns com.thelastcitadel.test.ideapad-storage
  (:require [com.thelastcitadel.ideapad-storage :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [ring.adapter.jetty :as jetty]
            [propS3t.core :as s3]))

(use-fixtures :once
              (fn [f]
                (let [j (jetty/run-jetty #'handler
                                         {:port 25156
                                          :join? false})]
                  (try
                    (f)
                    (finally
                      (.stop j))))))

(def storage (atom {}))

(deftest f
  (with-redefs [s3/write-stream (fn [_ b k stream & _]
                                  (swap! storage assoc (str b "/" k) (slurp stream)))
                s3/read-stream (fn [_ b k]
                                 (java.io.ByteArrayInputStream.
                                  (.getBytes (get @storage (str b "/" k)))))
                read-creds (constantly {})]
    (let [store (atom nil)
          id (atom nil)]
      (let [{:keys [cookies]} (http/post "http://localhost:25156/login"
                                         {:form-params {:username "root"
                                                        :password "password"}
                                          ;; redirects eat cookies
                                          :follow-redirects false})]
        (reset! store cookies))
      (let [{:keys [body]} (http/put "http://localhost:25156/store"
                                     {:body "foo"
                                      :cookies @store})]
        (reset! id body)
        (is body))
      (let [{:keys [body]} (http/post "http://localhost:25156/retrieve"
                                      {:form-params {:id @id}
                                       :cookies @store})]
        (is (= body "foo"))))))
