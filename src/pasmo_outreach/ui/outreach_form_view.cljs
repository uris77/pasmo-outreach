(ns pasmo-outreach.ui.outreach-form-view
  (:require [re-frame.core :refer [dispatch subscribe]]
            [cljs-pikaday.reagent :as pikaday]
            [reagent.core :as reagent]))

 
(def new-outreach-date (reagent/atom (js/Date.)))
(def new-outreach (reagent/atom {:people-met nil
                                 :errors {}}))

(defn reset-form []
  (reset! new-outreach-date (js/Date.))
  (reset! new-outreach {:people-met    nil
                        :people-tested nil
                        :comments      ""
                        :latitude      nil
                        :longitude     nil
                        :errors        {}}))

(defn hide-outreach-view [dom-event]
  (.preventDefault dom-event)
  (dispatch [:hide-create-form]))

(defn dissoc-in 
  [item path field]
  (update-in item path dissoc field))

(defn validate-outreach [new-outreach]
  (let [required-fields [:people-met :people-tested :longitude :latitude]]
    (doseq [f required-fields]
      (do
        (if (and (seq (get @new-outreach f))
                 (not (empty? (.trim (get @new-outreach f)))))
          (swap! new-outreach dissoc-in [:errors] f)
          (swap! new-outreach assoc-in [:errors f] "required"))))
    (if (seq (:errors @new-outreach))
      (swap! new-outreach assoc :valid? false)
      (swap! new-outreach assoc :valid? true))))

(defn save-new-outreach 
  [new-outreach date dom-event]
  (.preventDefault dom-event)
  (swap! new-outreach assoc :date @date)
  (validate-outreach new-outreach)
  (if (:valid? @new-outreach)
    (dispatch [:create-outreach @new-outreach])
    (.log js/console "Invalid....")))

(defn edit-new-outreach
  [outreach field dom-event]
  (swap! outreach assoc field (-> dom-event  .-target .-value)))

(defn create-outreach-view []
  [:div.container-fluid
   [:div.panel.panel-default
    [:div.panel-heading.clearfix
     [:h3.panel-title.pull-left "New Outreach"]
     [:div.btn-group.pull-right
      [:div.col-xs-5
       [:button.btn.btn-danger
        {:on-click hide-outreach-view}
        [:i.fa.fa-remove] "Cancel"]]
      [:div.col-xs-2
       [:button.btn.btn-primary
        {:on-click (partial save-new-outreach new-outreach new-outreach-date)}
        [:i.fa.fa-check] "Save"]]]]
    [:div.modal-body>form.form-horizontal
     [:div.form-group
      [:label.col-xs-3.control-label "Date"]
      [:div.col-xs-4
       [pikaday/date-selector {:input-attrs {:id "outreach-date"}
                               :date-atom   new-outreach-date}]]]
     [:div.form-group
      [:label.col-xs-3.control-label "# of people met"]
      [:div.col-xs-4
       [:input.form-control {:type      "text"
                             :value     (:people-met @new-outreach)
                             :on-change (partial edit-new-outreach new-outreach :people-met)}]]
      (when (and (not (:valid? @new-outreach))
                 (seq (get-in @new-outreach [:errors :people-met])))
        [:div.col-xs-5 [:span.label.label-danger "Required."]])]
     [:div.form-group
      [:label.col-xs-3.control-label "# of people tested"]
      [:div.col-xs-4
       [:input.form-control {:type      "text"
                             :value     (:people-tested @new-outreach)
                             :on-change (partial edit-new-outreach new-outreach :people-tested)}]]
      (when (and (not (:valid? @new-outreach))
                 (seq (get-in @new-outreach [:errors :people-met])))
        [:div.col-xs-5 [:span.label.label-danger "Required."]])]
     [:div.form-group
      [:label.col-xs-3.control-label "Longitude"]
      [:div.col-xs-4
       [:input.form-control {:type      "text"
                             :value     (:longitude @new-outreach)
                             :on-change (partial edit-new-outreach new-outreach :longitude)}]]
      (when (and (not (:valid? @new-outreach))
                 (seq (get-in @new-outreach [:errors :longitude])))
        [:div.col-xs-5 [:span.label.label-danger "Required."]])]
     [:div.form-group
      [:label.col-xs-3.control-label "Latitude"]
      [:div.col-xs-4
       [:input.form-control {:type      "text"
                             :value     (:latitude @new-outreach)
                             :on-change (partial edit-new-outreach new-outreach :latitude)}]]
      (when (and (not (:valid? @new-outreach))
                 (seq (get-in @new-outreach [:errors :latitude])))
        [:div.col-xs-5 [:span.label.label-danger "Required."]])]
     [:div.form-group
      [:label.col-xs-3.control-label "Comments"]
      [:div.col-xs-4
       [:textarea.form-control {:rows      8
                                :value     (:comments @new-outreach)
                                :on-change (partial edit-new-outreach new-outreach :comments)}]]]]]])


