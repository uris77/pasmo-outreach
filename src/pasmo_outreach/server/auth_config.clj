(ns pasmo-outreach.server.auth-config
  (:require [environ.core :refer [env]]
            [friend-oauth2.util :refer [format-config-uri]]
            [friend-oauth2.workflow :as oauth2]
            [taoensso.timbre :as log]))

(def oauth-domain  (env :oauth-domain))

(def oauth-callback (env :oauth-callback))

(def client-id (env :client-id))

(def client-secret (env :client-secret))

(def auth-url (env :auth-url))

(def token-url (env :token-url))


(def client-config
  {:client-id     client-id
   :client-secret client-secret
   :callback      {:domain oauth-domain :path oauth-callback}})

(defn uri-config [config]
  {:authentication-uri {:url   auth-url
                        :query {:client_id     (:client-id config)
                                :redirect_uri  (format-config-uri config)
                                :response_type "code"
                                :scope         "email"}}
   :access-token-uri   {:url   token-url
                        :query {:client_id     (:client-id config)
                                :client_secret (:client-secret config)
                                :grant_type    "authorization_code"
                                :redirect_uri  (format-config-uri config)}}})

(defn workflow 
  [config credential-fn]
  (oauth2/workflow
   {:client-config config
    :uri-config    (uri-config config)
    :credential-fn credential-fn}))

