(ns pasmo-outreach.server.core
  (:gen-class)
  (:require [taoensso.timbre :as log]
            [compojure.core :refer [routes]]
            [environ.core :refer [env]]
            [pasmo-outreach.server.db.core :as db]
            [pasmo-outreach.server.routes.core :refer [start-html-routes map->ApiRoutes]]
            [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]
            [pasmo-outreach.server.auth-config :as auth-config]
            [pasmo-outreach.server.routes.core :refer [credential-fn]]
            [cemerick.friend :as friend]
            [selmer.parser :refer [render-file]]
            [pasmo-outreach.server.utils :refer [new-uuid]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))


(defn stop-server [server]
  (when server (server)))

(defn unauth-handler [req]
  (render-file "templates/404.html" ))

(defrecord HttpServer [db-component api-routes html-routes]
  component/Lifecycle
  (start [this]
    (let [port          (env :server-port)
          auth-handlers (-> (routes (:handlers api-routes) (:handlers html-routes))
                            (friend/authenticate {:allow-annon? true
                                                  :workflows    [(auth-config/workflow auth-config/client-config (partial credential-fn (:db db-component)))]}))
          server        (run-server (wrap-defaults auth-handlers 
                                                   (assoc-in site-defaults [:security :anti-forgery] false)) 
                                    {:port port :join? false})]
      (log/info (str "Started server on port " port))
      (assoc this :server server)))
  
  (stop [this]
    (stop-server (:server this))
    (assoc this :server nil)))

(defn create-system
  [{mongo-uri :mongo-uri :as config}]
  (component/system-map
   :db-component (db/start-database mongo-uri)
   :html-routes (start-html-routes)
   :api-routes (component/using
                (map->ApiRoutes {})
                [:db-component])
   :app (component/using
         (map->HttpServer {})
         [:db-component :api-routes :html-routes])))

(defn -main
  [& args]
  (create-system {:mongo-uri (env :mongo-uri)}))

