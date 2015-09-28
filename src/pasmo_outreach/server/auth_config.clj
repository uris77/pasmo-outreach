(ns pasmo-outreach.server.auth-config
  (:require [environ.core :refer [env]]
            [friend-oauth2.util :refer [format-config-uri]]
            [cemerick.friend :as friend]
            [clojure.tools.logging :as log]
            [clj-http.client :as http-client]
            [pasmo-outreach.server.db.users :as user-db]))


(def default-admin (env :default-admin))

(def oauth-domain  (env :oauth-domain))

(def oauth-callback (env :oauth-callback))

(def client-id (env :client-id))

(def client-secret (env :client-secret))

(def auth-url (env :auth-url))

(def token-url (env :token-url))

(def callback-url (env :callback-url))

(defn new-uuid 
  "Generates a uuid."
  []
  (.toString (java.util.UUID/randomUUID)))

(defn fetch-google-user-info [token]
  (http-client/get (env :profile-url) {:headers {"Authorization" (str "Bearer " token)} :as :json}))

(defn user-access-token [req]
  (get-in (friend/current-authentication req) [:identity :access-token]))

(defn user-email-from-profile [profile]
  (:value (first (:emails profile))))

(defn add-token
  [email first-name last-name]
  (let [user (user-db/add-api-token email first-name last-name (new-uuid))]
    (log/info "Logged In: " user)
    (if (nil? user)
      {:rules #{}}
      user)))

(defn credential-fn [token]
  (let [access-token (:access-token token)
        profile (:body (fetch-google-user-info access-token))
        email (user-email-from-profile profile)
        first-name (get-in profile [:name :givenName])
        last-name (get-in profile [:name :familyName])]
    (if (= email default-admin)
      (add-token email first-name last-name)
      (add-token email first-name last-name))))


(def client-config
  {:client-id client-id
   :client-secret client-secret
   :callback  {:domain oauth-domain :path oauth-callback}})

(def uri-config
  {:authentication-uri {:url auth-url
                        :query {:client_id (:client-id client-config)
                                :redirect_uri (format-config-uri client-config)
                                :response_type "code"
                                :scope "email"}}
  :access-token-uri {:url token-url
                     :query {:client_id (:client-id client-config)
                             :client_secret (:client-secret client-config)
                             :grant_type "authorization_code"
                             :redirect_uri (format-config-uri client-config)}}})
