(ns pasmo-outreach.server.core
  (:gen-class)
  (:require [clojure.tools.logging :as log]
            [compojure.core :refer [defroutes routes GET]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :as resp]
            [hiccup.middleware :refer [wrap-base-url]]
            [hiccup.page :as h]
            [selmer.parser :refer [render-file]]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [cheshire.core :refer :all]
            [cemerick.friend :as friend]
            [friend-oauth2.workflow :as oauth2]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [clj-http.client :as http-client]
            [environ.core :refer [env]]
            [pasmo-outreach.server.auth-config :refer [client-config uri-config credential-fn]]
            [pasmo-outreach.server.db.core :as db]
            [pasmo-outreach.server.routes.core :refer [sample-routes]]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]))

(defn init! []
  (cheshire.generate/add-encoder org.bson.types.ObjectId cheshire.generate/encode-str)
  (db/mongo-connection!)
  (log/info "pasmo-outreach is starting"))

(defn destroy! []
  (db/disconnect!)
  (println "pasmo-gigi-clj is shutting down"))

(defn index-handler [req]
  (render-file "templates/index.html" {:dev {env :dev?}}))

(defroutes public-routes
  (GET "/" req index-handler))

(def api-app
  (-> sample-routes
      wrap-json-body
      wrap-json-response))

(def authenticated-app
  (-> (routes api-app)
      (friend/authenticate {:allow-annon?         true
                            :login-uri            "/login"
                            :default-landing-uri  "/sample"
                            :unauthorized-handler #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                                       resp/response
                                                       (resp/status 401))
                            :workflows            [(oauth2/workflow
                                                    {:client-config client-config
                                                     :uri-config    uri-config
                                                     :credential-fn credential-fn})]})))

(def site-and-api
  (wrap-defaults (routes public-routes authenticated-app) (assoc-in site-defaults [:security :anti-forgery] false)))

(defn start-server
  [handler port]
  (init!)
  (let [server (run-server handler {:port port :join? false})]
    (log/info "Started server on port " port)
    server))

(defn stop-server
  [server]
  (when server
    (do
      (destroy!)
      (server)))) ;; run-server returns a function that stops itself.

(defrecord PasmoOutreach
    []
  component/Lifecycle
  (start [this]
    (let [port (Integer/parseInt (or (System/getenv "PORT") "3449"))]
      (assoc this :server (start-server #'site-and-api port))))

  (stop [this]
    (stop-server (:server this))
    (dissoc this :server)))

(defn create-system
  []
  (PasmoOutreach.))

(defn -main
  [& args]
  (.start (create-system)))


