(ns repl
  (:require [reloaded.repl :refer [system reset stop]]
            [environ.core :refer [env]]
            [pasmo-outreach.server.core]))

(defn create-system []
  (pasmo-outreach.server.core/create-system {:mongo-uri (env :mongo-uri)}))

#_(reloaded.repl/set-init! #'pasmo-outreach.server.core/create-system)
(reloaded.repl/set-init! #'create-system)
