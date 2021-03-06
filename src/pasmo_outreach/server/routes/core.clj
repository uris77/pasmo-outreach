(ns pasmo-outreach.server.routes.core
  (:require [compojure.core :refer [defroutes GET POST DELETE routes context]]
            [compojure.route :refer [not-found resources]]
            [com.stuartsierra.component :as component]
            [ring.middleware
             [defaults :refer [site-defaults wrap-defaults]]
             [json :refer [wrap-json-body wrap-json-response wrap-json-params]]
             [params :refer [assoc-query-params]]]
            [ring.middleware.format :refer [wrap-restful-format]]
            [pasmo-outreach.server.db.users :as user-db]
            [pasmo-outreach.server.db.outreach :as outreach]
            [selmer.parser :refer [render-file]]
            [cemerick.friend :refer [authorize authenticated current-authentication]]
            [environ.core :refer [env]]
            [taoensso.timbre :as log]
            [clj-http.client :as http-client]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Authentication
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn new-uuid 
  "Generates a uuid."
  []
  (.toString (java.util.UUID/randomUUID)))

(defn fetch-google-user-info [token]
  (http-client/get (env :profile-url) {:headers {"Authorization" (str "Bearer " token)} :as :json}))

(defn user-access-token [req]
  (get-in (current-authentication req) [:identity :access-token]))

(defn user-email-from-profile [profile]
  (:value (first (:emails profile))))

(defn add-token
  [db email first-name last-name]
  (let [user (user-db/add-api-token db email first-name last-name (new-uuid))]
    (log/info "Logged In: " user)
    (if (nil? user)
      {:roles #{::user}}
      user)))

(defn credential-fn [db token]
  (let [access-token (:access-token token)
        profile      (:body (fetch-google-user-info access-token))
        email        (user-email-from-profile profile)
        first-name   (get-in profile [:name :givenName])
        last-name    (get-in profile [:name :familyName])]
    (add-token db email first-name last-name)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; Helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def json-resp {:headers {"Content-Type" "application/json"}})

(defn with-keywords [m]
  (into {}
        (for [[k v] m]
          [(keyword k) (if (map? v) (with-keywords v) v)])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; API ROUTES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-outreach
  [db req]
  (let [params (with-keywords (:json-params req))
        res    (->> (assoc params :user (:email (current-authentication))) (outreach/create db))]
    {:body res}))

(defn list-outreach 
  [db req]
  (let [params (with-keywords (:query-params req))
        page   (Integer/parseInt (:page params))
        ls     (outreach/all db (inc page))]
    {:body {:list         ls
            :current-page page
            :total        (outreach/total-records db)}}))

(defn find-by-id 
  [db id req]
  (let [outreach (outreach/find-by-id db id)]
    {:body {:outreach outreach}}))

(defn delete-by-id
  [db id]
  (outreach/delete-by-id db id)
  {:body "OK"})

(defn api-handlers 
  [db-component]
  (let [db (:db db-component)]
    (defroutes api-routes
      (context "/api/outreach" []
               (defroutes outreach-routes
                 (GET "/" req (authorize #{:user} (list-outreach db (assoc-query-params req "UTF-8"))))
                 (POST "/" req (authorize #{:user} (create-outreach db req))))
               (context "/:id" [id]
                        (defroutes outreach-with-id
                          (GET "/" req (authorize #{:user} (find-by-id db id req)))
                          (DELETE "/" _ (authorize #{:user} (delete-by-id db id)))))))))

(defrecord ApiRoutes [db-component]
  component/Lifecycle
  (start [this]
    (let [handlers (-> db-component
                       api-handlers
                       (routes)
                       (wrap-json-body {:keywords? true :bigdecimals? true})
                       (wrap-json-params)
                       (wrap-json-response)
                       (wrap-restful-format))]
      (cheshire.generate/add-encoder org.bson.types.ObjectId cheshire.generate/encode-str)
      (assoc this :db-component db-component :handlers handlers)))

  (stop [this]
    (dissoc this :db-component :handlers)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; HTML Routes
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn index-handler [req]
  (render-file "templates/index.html" {:dev {env :dev?}}))
(defn start-handler [req]
  (render-file "templates/main.html" {:dev {env :dev?}}))

(defn html-handlers []
  (defroutes html-routes
    (GET "/" req (start-handler req))
    (GET "/app" req (authorize #{:user} (index-handler req)))
    (resources "/")
    (not-found "Not Found")))

(defrecord HtmlRoutes []
  component/Lifecycle
  (start [this]
    (let [handlers (-> (html-handlers)
                       routes)]
      (log/info "Starting HTML routes")
      (assoc this :handlers handlers)))
  
  (stop [this]
    (dissoc this :handlers)))

(defn start-html-routes []
  (map->HtmlRoutes {}))

