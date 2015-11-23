(defproject pasmo-outreach "1.0.0-SNAPSHOT"
  :description "Basic CRUD for gathering information on outreach efforts."
  :url "http://outreach.pasmo.bz"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [com.stuartsierra/component "0.3.0"]
                 [environ "1.0.1"]
                 [compojure "1.4.0"]
                 [cheshire "5.5.0"]
                 [selmer "0.9.2"]
                 [com.novemberain/monger "3.0.0"]
                 [http-kit "2.1.18"]
                 [ring-server "0.4.0" :exclusions [org.eclipse.jetty/jetty-http
                                                   org.eclipse.jetty/jetty-continuation]]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [com.cemerick/friend "0.2.1" :exclusions [ring/ring-core
                                                           org.clojure/core.cache
                                                           org.apache.httpcomponents/httpclient]]
                 [reagent "0.5.1" :exclusions [com.google.guava/guava]]
                 [reagent-utils "0.1.5" :exclusions [com.google.guava/guava]]
                 [re-frame "0.5.0" :exclusions [org.clojure/core.cache
                                                com.google.guava/guava]]
                 [org.clojure/core.cache "0.6.4"]
                 [cljs-http "0.1.37"]
                 [secretary "1.2.3"]
                 [friend-oauth2 "0.1.3" :exclusions [commons-logging 
                                                     slingshot
                                                     org.apache.httpcomponents/httpcore]]
                 [clj-time "0.11.0"]
                 [clj-jwt "0.1.1"]
                 [com.taoensso/timbre "4.1.4"]
                 [clj-http "3.0.0-SNAPSHOT" :exclusions [slingshot
                                                         commons-logging]]
                 [ring-middleware-format "0.6.0"]
                 [cljs-pikaday "0.1.2"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]]

  :jvm-opts ["-Xmx512m"]
  
  :plugins [[lein-ring "0.9.6"]
            [lein-environ "1.0.1"]]

  :min-lein-version "2.0.0"
  
  :uberjar-name "pasmo-outreach.jar"

  
  :profiles {:dev-common       {:plugins       [[lein-cljsbuild "1.1.1-SNAPSHOT"]
                                                [lein-figwheel "0.5.0-SNAPSHOT"]]
                                :dependencies  [[reloaded.repl "0.2.0"]]
                                :env           {:dev? true}
                                :main          pasmo-outreach.server.core
                                :open-browser? true
                                :source-paths  ["dev" "src/pasmo_outreach/server"]
                                :cljsbuild     {:builds [{:source-paths ["src/pasmo_outreach/ui"]
                                                          :figwheel     {:on-jsload "pasmo-outreach.ui.core/on-reload"}
                                                          :compiler     {:output-to            "target/classes/public/js/app.js"
                                                                         :output-dir           "target/classes/public/js/out"
                                                                         :asset-path           "js/out"
                                                                         :optimizations        :none
                                                                         :recompile-dependents true
                                                                         :main                 "pasmo-outreach.ui.core"
                                                                         :externs              ["resources/public/js/externs.js"]
                                                                         :source-map           true}}]}}
             :dev-env-vars     {}
             :dev              [:dev-env-vars :dev-common]

             :uberjar-common   {:aot          :all
                                :omit-source  true
                                :source-paths ["src/pasmo_outreach/server"]
                                :main         pasmo-outreach.server.core
                                :env          {:dev? false}
                                :hooks        [leiningen.cljsbuild]
                                :cljsbuild    {:builds {:app {:source-paths ["src/pasmo_outreach/ui"]
                                                              :jar          true
                                                              :figwheel     false
                                                              :compiler     {:optimizations  :advanced
                                                                             :main           "pasmo-outreach.ui.core"
                                                                             :output-wrapper true
                                                                             :asset-path     "js/out"
                                                                             :output-to      "target/classes/public/js/app.js"
                                                                             :output-dir     "target/classes/public/js/out"
                                                                             :externs        ["resources/public/js/externs.js"]}}}}}
             :uberjar-env-vars {:mongo-uri      (System/getenv "MONGO_URI")
                                :db             (System/getenv "DB")
                                :default-admin  (System/getenv "DEFAULT_ADMIN")
                                :client-id      (System/getenv "CLIENT_ID")
                                :client-secret  (System/getenv "CLIENT_SECRET")
                                :oauth-callback (System/getenv "OAUTH_CALLBACK")
                                :auth-url       (System/getenv "AUTH_URL")
                                :token-url      (System/getenv "TOKEN_URL")
                                :profile-url    (System/getenv "PROFILE_URL")}
             :uberjar          [:uberjar-common :uberjar-env-vars]})

