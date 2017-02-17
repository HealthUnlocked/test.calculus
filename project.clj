(defproject healthunlocked/test.calculus "0.1.0"
  :description "Integration tests using test.check"
  :url "http://github.com/healthunlocked/test.calculus"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[com.gfredericks/test.chuck "0.2.7"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/test.check "0.9.0"]]
  :profiles {:dev {:source-paths ["dev/src"]
                   :resource-paths ["dev/resources"]
                   :dependencies [[clj-time "0.13.0"]
                                  [environ "1.1.0"]
                                  [mysql/mysql-connector-java "6.0.5"]
                                  [org.clojure/java.jdbc "0.6.1"]
                                  [prismatic/schema "1.1.3"]
                                  [prismatic/schema-generators "0.1.0"]]
                   :plugins [[healthunlocked/lein-docker-compose "0.1.0"]
                             [lein-ancient "0.6.10"]
                             [lein-environ "1.1.0"]]}})
