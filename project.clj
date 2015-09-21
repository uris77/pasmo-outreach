(defproject pasmo-outreach "1.0.0-SNAPSHOT"
  :description "Basic CRUD for gathering information on outreach efforts."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.58"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.stuartsierra/component "0.3.0"]
                 [environ "1.0.1"]
                 [reloaded.repl "0.2.0"]
                 [compojure "1.4.0"]
                 [cheshire "5.5.0"]
                 [selmer "0.9.2"]
                 [com.novemberain/monger "3.0.0"]
                 [http-kit "2.2.0-SNAPSHOT"]
                 [ring-server "0.4.0" :exclusions [org.eclipse.jetty/jetty-http
                                                   org.eclipse.jetty/jetty-continuation]]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [com.cemerick/friend "0.2.2-SNAPSHOT" :exclusions [ring/ring-core
                                                                    org.clojure/core.cache
                                                                    org.apache.httpcomponents/httpclient]]
                 [reagent "0.5.1"]
                 [reagent-utils "0.1.5"]
                 [re-frame "0.5.0-SNAPSHOT"]
                 [cljs-http "0.1.37"]
                 [secretary "2.0.0-SNAPSHOT"]]

  :jvm-opts ["-Xmx512m"]
  
  :plugins [[lein-ring "0.9.6"]
            [lein-environ "1.0.0"]]

  :min-lein-version "2.0.0"

  :main pasmo-outreach.server
  
  :uberjar-name "pasmo-outreach.jar")
