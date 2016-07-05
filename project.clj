(defproject clj-airbnb "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.374"]
                 [environ "1.0.3"]
                 [clj-http "2.2.0"]
                 [cheshire "5.6.1"]
                 [ring "1.5.0"]
                 [ring/ring-defaults "0.1.5"]
                 [compojure "1.4.0"]
                 [clj-time "0.12.0"]
                 [ring.middleware.logger "0.5.0"] ; need?
                 [com.draines/postal "2.0.0"]
                 [com.novemberain/monger "3.0.1"]]
  :main ^:skip-aot clj-airbnb.init
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
