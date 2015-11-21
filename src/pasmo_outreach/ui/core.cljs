(ns pasmo-outreach.ui.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [pasmo-outreach.ui.routes :as routes :refer [current-page]]))

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn ^:export init []
  (routes/app-routes)
  (mount-root))

(defn on-reload []
  (mount-root))
