(ns com.thelastcitadel.test.ideapad-storage
  (:require [com.thelastcitadel.ideapad-storage :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [ring.adapter.jetty :as jetty]))

(use-fixtures :once
              (fn [f]
                (let [j (jetty/run-jetty #'handler
                                         {:port 25156
                                          :join? false})]
                  (try
                    (f)
                    (finally
                      (.stop j))))))

(deftest f
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
      (is (= body "foo")))))
