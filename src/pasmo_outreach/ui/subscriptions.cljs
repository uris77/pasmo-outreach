(ns pasmo-outreach.ui.subscriptions
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub
 :outreach-list
 (fn [app-state _]
   (reaction (:outreach-list @app-state))))

(register-sub
 :loading-outreach-list?
 (fn [app-state _]
   (reaction (:loading? @app-state))))

(register-sub
 :creating-outreach?
 (fn [app-state _]
   (reaction (:creating-new-outreach? @app-state))))

