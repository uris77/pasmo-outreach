(ns pasmo-outreach.server.db.outreach
  (:import org.bson.types.ObjectId)
  (:require [monger.collection :as mc]
            [monger.query :as mq]))

(def coll "outreach")

(defn coords->location [latitude longitude]
  {:type        "Point"
   :coordinates [longitude latitude]})

(defn location->coords [location]
  (let [coordinates (:coordinates location)]
    {:longitude (first coordinates) :latitude (second coordinates)}))

(defn pprint-outreach
  [persisted-outreach]
  (let [coordinates (location->coords (:location persisted-outreach))
        id (:_id persisted-outreach)]
    (-> persisted-outreach
        (dissoc :location)
        (assoc :longitude (:longitude coordinates))
        (assoc :latitude (:latitude coordinates))
        (assoc :id id))))

(defn total-records [db]
  (mc/count db coll))

(defn all 
  [db page]
  (let [ls (mq/with-collection db coll
             (mq/find {})
             (mq/sort {:date -1})
             (mq/paginate :page page :per-page 5))]
    (map pprint-outreach ls)))

(defn find-by-date
  [db date]
  (let [outreach (mc/find-maps db coll {:date date})]
    (first (map #(assoc % :_id (.toString (:_id %))) outreach))))

(defn validate-user-params [params]
  (let [required [:latitude :longitude :people-met :people-tested :date]]
    (reduce (fn [acc it]
              (if (or (nil? (get params it))
                      (empty? (get params it)))
                (conj acc {it "can not be empty."})
                acc))
            []
            required)))
(defn delete-by-id 
  [db id]
  (let [oid (ObjectId. id)]
    (mc/remove-by-id db coll oid)))

(defn create
  [db params-map]
  (let [errors   (validate-user-params params-map)
        outreach (find-by-date db (:date params-map))]
    (if (seq? errors)
      {:errors errors :entity nil}
      (let [location       (coords->location (Double/parseDouble (:latitude params-map)) (Double/parseDouble (:longitude params-map)))
            params-to-save (select-keys params-map [:date :people-met :people-tested :comments])
            new-outreach   (mc/insert-and-return db coll (assoc params-to-save :location location))]
        {:errors nil :entity (pprint-outreach new-outreach)}))))

(defn find-by-id
  [db id]
  (let [oid (ObjectId. id)
        outreach (mc/find-one-as-map db coll {:_id oid})]
    (if outreach
      (pprint-outreach outreach)
      outreach)))


