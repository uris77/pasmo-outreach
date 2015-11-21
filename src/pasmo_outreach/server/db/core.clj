(ns pasmo-outreach.server.db.core
  (:require [taoensso.timbre :as log]
            [com.stuartsierra.component :as component]
            [monger.core :as mg]))

(defrecord Database [mongo-uri]
  component/Lifecycle
  (start [this]
    (let [{:keys [conn db]} (mg/connect-via-uri mongo-uri)]
      (log/info "Connected to mongo.")
      (assoc this :db db :conn conn)))
  
  (stop [this]
    (when-let [conn (:conn this)]
      (mg/disconnect conn))
    (log/info "Disconnected from mongo.")
    (dissoc this :db :conn)))

(defn start-database [mongo-uri]
  (log/info "Will attempt to connect to " mongo-uri)
  (map->Database {:mongo-uri mongo-uri}))

