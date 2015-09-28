(ns pasmo-outreach.server.routes.core
  (:require [compojure.core :refer [defroutes GET]]
            [pasmo-outreach.server.db.users :as user-db]
            [cemerick.friend :refer [authorize]]))

(def json-resp {:headers {"Content-Type" "application/json"}})

(defroutes sample-routes
  (GET "/sample" req 
       (authorize #{:pasmo-outreach.server.db.users/user} (assoc json-resp :body (user-db/all)))))

