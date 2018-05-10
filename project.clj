(defproject erl-like-app "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.8.0"]

                 [org.clojure/core.match "0.3.0-alpha4"]

                 [otplike "0.3.0-alpha"]

                 [com.taoensso/timbre "4.8.0"]              ; logging

                 ]

  :uberjar-name "erl-like-app.jar"

  :main erl-like-app.server

  :source-paths ["src"]

  :resource-paths ["resources"]

  :profiles {
             :dev     {:source-paths ["env/dev"]}

             :uberjar {:env {:production "true"}
                       :omit-source true}})
