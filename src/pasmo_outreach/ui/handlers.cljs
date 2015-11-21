(ns pasmo-outreach.ui.handlers
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [re-frame.core :refer [register-handler dispatch]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [pasmo-outreach.ui.state :refer [app-db]]))

(defn fetch-outreach-list [page]
  (go
    (let [url (str "/api/outreach?page=" page)
          resp (<! (http/get url {"accept" "application/json"}))]
      (dispatch [:received-outreach-list (:body resp)]))))

(defn create-outreach [params]
  (go
    (let [resp (<! (http/post (str "/api/outreach") {:json-params params}))]
      (dispatch [:created-outreach (:body resp)]))))

(register-handler
 :initialize-db
 (fn [_ _]
   app-db))

(register-handler
 :fetch-outreach-list
 (fn [app-state _]
   (fetch-outreach-list (get-in app-state [:outreach-list :next-page]))
   (assoc app-state :loading? true)))

(register-handler
 :received-outreach-list
 (fn [app-state [_ server-resp]]
   (let [outreach-list (:list server-resp)
         next-page (get-in app-state [:outreach-list :next-page])]
     (-> app-state
         (assoc-in [:outreach-list :list] outreach-list)
         (assoc-in [:outreach-list :total] (:total server-resp))
         (assoc-in [:outreach-list :next-page] (inc next-page))
         (assoc :loading? false)))))

(register-handler
 :show-create-form
 (fn [app-state _]
   (assoc app-state :creating-new-outreach? true)))

(register-handler
 :hide-create-form
 (fn [app-state _]
   (assoc app-state :creating-new-outreach? false :new-outreach {:date (js/Date.)})))

(register-handler
 :create-outreach
 (fn [app-state [_ outreach]]
   (create-outreach (dissoc outreach :errors))
   (assoc app-state :saving? true)))

(register-handler
 :created-outreach
 (fn [app-state [_ outreach]]
   (let [new-ls (conj (:outreach-list app-state) (:entity outreach))]
     (assoc app-state :saving? false :creating-new-outreach? false :outreach-list new-ls))))


