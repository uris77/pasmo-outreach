(ns pasmo-outreach.ui.views
  (:require [re-frame.core :refer [dispatch subscribe]]
            [cljs-pikaday.reagent :as pikaday]
            [reagent.core :as reagent]
            [pasmo-outreach.ui.nav :refer [navbar]]
            [pasmo-outreach.ui.outreach-form-view :refer [create-outreach-view reset-form]]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]))

(def date-formatter (tf/formatters :date-time))
(def pretty-date-formatter (tf/formatter "EEE dd MMM yyyy"))

(defn goto-previous
  [dom-event]
  (.preventDefault dom-event)
  (dispatch [:goto-previous-page]))

(defn goto-next
  [dom-event]
  (.preventDefault dom-event)
  (dispatch [:goto-next-page]))

(defn pagination-panel []
  (fn []
    (let [pages       (subscribe [:pagination-pages])
          left-arrow  (if (< 0 (:previous @pages)) false true)
          right-arrow (if (= (:max-pages @pages) (:next @pages)) true false)]
      [:div {:style {:margin-bottom "1em"}}
       [:div.row
        [:btn.btn.btn-default.btn-lg {:disabled left-arrow
                                      :on-click goto-previous} 
         [:span.glyphicon.glyphicon-menu-left]]
        [:span {:style {:margin-left "1em"}}
         [:label (str "Total Records: "(:total @pages))]
         [:label {:style {:margin-left "2em"}} (str "Page: " (:current-page @pages)) ]]
        [:btn.btn.btn-default.btn-lg.pull-right  {:disabled right-arrow
                                                  :on-click goto-next} 
         [:span.glyphicon.glyphicon-menu-right]]]])))

(defn delete-outreach
  [outreach dom-event]
  (.preventDefault dom-event)
  (when (js/confirm "Are you sure you want to delete this outreach?")
    (dispatch [:delete-outreach outreach])))

(defn outreach-list []
  (let [ls (subscribe [:outreach-list])]
    (fn []
      (if (seq (:list @ls))
        [:div.row {:style {:margin-left "0.1em"}}
         [pagination-panel]
         [:div.row>div.bootcards-list>div.panel.panel-default>div.list-group
          [:div.list-group-item>h4.list-group-item-heading "Outreach List"]
          (for [outreach (:list @ls)]
            (let [id   (:id outreach)
                  date (tf/parse date-formatter (:date outreach))]
              ^{:key id} [:a.list-group-item {:href "#"}
                          [:h4.list-group-item-heading (tf/unparse pretty-date-formatter date)]
                          [:p.list-group-item-text
                           [:div>span (str "People Met: " (:people-met outreach) " | People Tested: " (:people-tested outreach))]]
                          [:p.list-group-item-text
                           [:div>span (str "Coordinates | Latitude: " (:latitude outreach) " | Longitude: " (:longitude outreach))]]
                          [:p.list-group-item-text
                           [:div>span (str "Comments: " (:comments outreach))]]
                          [:p.list-grou-item-text
                           [:div>span
                            [:btn.btn-default.btn-danger.btn-lg {:on-click (partial delete-outreach outreach)}
                             [:span.glyphicon.glyphicon-trash]]]]]))]]
        [:div.row
         [:h2 "No outreach has been recorded yet."]]))))

(defn app []
  (let [ls                     (subscribe [:outreach-list])
        creating-new-outreach? (subscribe [:creating-outreach?])]
    (fn []
      [:div.row.container-fluid {:style {:margin-left "0.5em"}}
       [:div.container.col-xs-10
        (if @creating-new-outreach?
          (do 
            (reset-form)
            [create-outreach-view])
          [outreach-list])]])))

(defn main-panel []
  [:div
   [navbar]
   [app]])

