(ns repl
  (:require [reloaded.repl :refer [system reset stop]]
            [pasmo-outreach.server.core]))

(reloaded.repl/set-init! #'pasmo-outreach.server.core/create-system)

