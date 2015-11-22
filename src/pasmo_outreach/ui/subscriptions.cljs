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

(register-sub
 :pagination-pages
 (fn [app-state _]
   (let [next      (get-in @app-state [:outreach-list :next-page])
         total     (get-in @app-state [:outreach-list :total])
         max-pages (.ceil js/Math (/ total (:page-size @app-state)))
         current   (get-in @app-state [:outreach-list :current-page])
         previous  current]
     (reaction {:next next :previous previous :max-pages max-pages :total total :current-page (inc current)}))))

