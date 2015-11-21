(ns pasmo-outreach.server.utils)

(defn new-uuid 
  "Generates a uuid."
  []
  (.toString (java.util.UUID/randomUUID)))

