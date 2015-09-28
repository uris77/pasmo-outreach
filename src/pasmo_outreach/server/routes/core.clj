(ns pasmo-outreach.server.routes.core
  (:require [compojure.core :refer [defroutes GET]]))

(def json-resp {:headers {"Content-Type" "application/json"}})

(defroutes sample-routes
  (GET "/sample" req (assoc json-resp :body [1 2 3 4])))

