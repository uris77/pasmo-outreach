(ns pasmo-outreach.server.db.outreach
  (:require [monger.collection :as mc]
            [monger.query :as mq]))

(def coll "outreach")

(defn total-records [db]
  (mc/count db coll))

(defn all 
  [db page]
  (let [ls (mq/with-collection db coll
             (mq/find {})
             (mq/sort {:date -1})
             (mq/paginate :page page :per-page 5))]
    (map #(assoc % :_id (.toString (:_id %))) ls)))

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

(defn create
  [db params-map]
  (let [errors   (validate-user-params params-map)
        outreach (find-by-date db (:date params-map))]
    (if (seq? errors)
      {:errors errors :entity nil}
      (let [new-outreach (mc/insert-and-return db coll params-map)]
        {:errors nil :entity (assoc new-outreach :id (:_id new-outreach))}))))


