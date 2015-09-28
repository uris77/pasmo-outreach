(ns pasmo-outreach.server.routes.core
  (:require [compojure.core :refer [defroutes GET]]
            [pasmo-outreach.server.db.users :as user-db]))

(def json-resp {:headers {"Content-Type" "application/json"}})

(defroutes sample-routes
  (GET "/sample" req (assoc json-resp :body (user-db/all))))

