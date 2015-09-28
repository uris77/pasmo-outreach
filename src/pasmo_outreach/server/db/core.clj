(ns pasmo-outreach.server.db.core
  (:require [clojure.tools.logging :as log]
            [environ.core :refer [env]]
            [monger.core :as mg]))

(def mongo-connection (atom {}))

(defn connect!
  []
  (let [uri (env :mongo-uri)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    (log/info "Connected to mongo!")
    {:conn conn :db db}))

(defn disconnect!
  []
  (if (:conn @mongo-connection)
    (do
      (mg/disconnect (:conn @mongo-connection))
      (reset! mongo-connection {})))
  (log/info "Disconnected from mongo!"))

(defn start-connection!
  []
  (reset! mongo-connection (connect!))
  @mongo-connection)


(defn mongo-connection!
  []
  (if (empty? @mongo-connection)
    (do
      (reset! mongo-connection (connect!))
      @mongo-connection)
    @mongo-connection))

