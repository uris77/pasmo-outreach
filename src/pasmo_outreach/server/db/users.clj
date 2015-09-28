(ns pasmo-outreach.server.db.users
  (:import org.bson.types.ObjectId)
  (:require [environ.core :refer [env]]
            [monger.core :as mg]
            [monger.collection :as coll]
            [monger.operators :refer [$set]]
            [pasmo-outreach.server.db.core :refer [mongo-connection!]]))

(def mconn (mongo-connection!))
(def db (:db mconn))
(def conn (:conn mconn))
(def users-coll "pasmo_users")

(defn find-user [email]
  (let [user (coll/find-one-as-map db users-coll {:email email})]
    (when user
      (assoc user :roles (map #(keyword %) (:roles user))))))

;; {:user :first-name :errors {:email :first-name :last-name}}
(defn- validate-field [user-map field]
  (if (empty? (get user-map field))
    {field (str field " can not be empty!")}))

(defn rev-merge [k m]
  (merge m k))

(defn validate-user [user]
  (let [errors {}]
    (->> errors
         (rev-merge (validate-field user :first-name))
         (rev-merge (validate-field user :last-name))
         (rev-merge (validate-field user :email)))))

(defn create-user [user-map]
  (let [user (find-user (:email user-map))
        errors (validate-user user-map)]
    (if (or (empty? errors) (nil? user))
      (assoc  (coll/insert-and-return db users-coll user-map) :roles #{::user})
      errors)))

(defn update-user
  [oid updateq]
  (coll/update-by-id db users-coll oid updateq)
  (let [user (coll/find-map-by-id db users-coll oid)]
    (assoc user :roles #{::user})))

(defn all 
  "List all users."
  []
  (coll/find-maps db users-coll))

(defn add-api-token [email first-name last-name token]
  (let [user (find-user email)]
    (if (nil? user)
      (create-user {:email email :first-name first-name :last-name last-name :api-token token :roles #{::user}})
      (update-user (:_id user) {$set {:api-token token}}))))

(defn remove-user
  [id]
  (let [oid (ObjectId. id)] 
    (coll/remove-by-id db users-coll oid)))

