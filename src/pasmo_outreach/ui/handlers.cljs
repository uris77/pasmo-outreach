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
      (when (not= 500 (:status resp))
        (dispatch [:created-outreach (:body resp)])))))

(defn delete-outreach [outreach]
  (go
    (let [url  (str "/api/outreach/" (:id outreach))
          resp (<! (http/delete url {"accept" "application/json"}))]
      (when (= 200 (:status resp))
        (dispatch [:deleted-outreach outreach])))))

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
         next-page     (get-in app-state [:outreach-list :next-page])
         current-page  (js/parseInt (:current-page server-resp))]
     (-> app-state
         (assoc-in [:outreach-list :list] outreach-list)
         (assoc-in [:outreach-list :total] (:total server-resp))
         (assoc-in [:outreach-list :next-page] (inc current-page))
         (assoc-in [:outreach-list :current-page] current-page)
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
   (let [new-ls (conj (get-in app-state [:outreach-list :list]) (:entity outreach))
         total  (get-in app-state [:outreach-list :total])]
     (-> app-state
         (assoc :saving? false :creating-new-outreach? false)
         (assoc-in [:outreach-list :total] (inc total))
         (assoc-in [:outreach-list :list] new-ls)))))

(register-handler
 :goto-previous-page
 (fn [app-state _]
   (let [current-page (get-in app-state [:outreach-list :current-page])
         total (get-in app-state [:outreach-list :total])
         max-pages (.ceil js/Math (/ total (:page-size app-state)))
         prev-page (dec current-page)
         has-prev-page? (>= 0 current-page)]
     (when (> current-page 0)
       (do
         (fetch-outreach-list prev-page)
         (assoc app-state :loading? true)))
     app-state)))

(register-handler
 :goto-next-page
 (fn [app-state _]
   (let [current-page   (get-in app-state [:outreach-list :current-page])
         total          (get-in app-state [:outreach-list :total])
         max-pages      (.ceil js/Math (/ total (:page-size app-state)))
         has-next-page? (>= (dec max-pages) (inc current-page))]
     (when has-next-page?
       (do
         (fetch-outreach-list (get-in app-state [:outreach-list :next-page]))
         (assoc app-state :loading? true)))
     app-state)))

(register-handler
 :delete-outreach
 (fn [app-state [_ outreach]]
   (delete-outreach outreach)
   app-state))

(register-handler
 :deleted-outreach
 (fn [app-state [_ outreach]]
   (let [outreach-list (get-in app-state [:outreach-list :list])
         new-list      (filter #(not= (:id %) (:id outreach)) outreach-list)
         total         (get-in app-state [:outreach-list :total])]
     (-> app-state
         (assoc-in [:outreach-list :list] new-list)
         (assoc-in [:outreach-list :total] (dec total))))))

