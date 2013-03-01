(defproject com.thelastcitadel/ideapad-storage "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.cemerick/friend "0.1.3"]
                 [hiccup "1.0.2"]
                 [props3t "0.0.4-SNAPSHOT"]
                 [compojure "1.1.5"]]
  :profiles {:dev {:dependencies [[ring "1.1.1"]
                                  [clj-http "0.5.5"]
                                  [cheshire "5.0.2"]]}}
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler com.thelastcitadel.ideapad-storage/handler})
