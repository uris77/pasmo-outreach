(ns pasmo-outreach.ui.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [cljs-pikaday.reagent :as pikaday]
            [reagent.core :as reagent]
            [pasmo-outreach.ui.nav :refer [navbar]]
            [pasmo-outreach.ui.outreach-form-view :refer [create-outreach-view]]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]))

(def date-formatter (tf/formatters :date-time))
(def pretty-date-formatter (tf/formatter "EEE dd MMM yyyy"))

(defn outreach-list []
  (let [ls (subscribe [:outreach-list])]
    (fn []
      [:div.row {:style {:margin-left "0.1em"}}
       [:div.row>div.bootcards-list>div.panel.panel-default>div.list-group
        [:div.list-group-item>h4.list-group-item-heading "Outreach List"]
        (for [outreach @ls]
          (let [id   (:id outreach)
                date (tf/parse date-formatter (:date outreach))]
            ^{:key id} [:a.list-group-item {:href "#"}
                        [:h4.list-group-item.heading (tf/unparse pretty-date-formatter date)]
                        [:p.list-group-item-text
                         [:div>span "Hey"]]]))]])))

(defn app []
  (let [ls                     (subscribe [:outreach-list])
        creating-new-outreach? (subscribe [:creating-outreach?])]
    (fn []
      [:div.row.container-fluid {:style {:margin-left "0.5em"}}
       [:div.container.col-xs-10
        (if @creating-new-outreach?
          [create-outreach-view]
          [outreach-list])]])))

(defn main-panel []
  [:div
   [navbar]
   [app]])

