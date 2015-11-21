(ns pasmo-outreach.ui.routes
  (:import goog.History)
  (:require-macros [secretary.core :refer [defroute]])
  (:require [re-frame.core :as re-frame]
            [secretary.core :as secretary]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [pasmo-outreach.ui.views :refer [main-panel]]
            [pasmo-outreach.ui.handlers]
            [pasmo-outreach.ui.subscriptions]))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn current-page []
  [(session/get :current-page)])

(defn app-routes
  []
  (.log js/console "IN ROUTES....")
  (secretary/set-config! :prefix "#")
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:fetch-outreach-list])

  (defroute index "/" []
    (session/put! :current-page #'main-panel))

  (hook-browser-navigation!))

