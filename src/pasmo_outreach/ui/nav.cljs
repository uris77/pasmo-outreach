(ns pasmo-outreach.ui.nav
  (:require [re-frame.core :refer [dispatch subscribe]]))

(defn select-create-form [dom-event]
  (.preventDefault dom-event)
  (.log js/console "DISPATCHING")
  (dispatch [:show-create-form]))

(defn navbar []
  (fn []
    [:div.navbar.navbar-default.navbar-fixed-top {:role "navigation"}
     [:div.container-fluid
      [:div.navbar-header
       [:button.navbar-toggle.collapsed {:type          "button"
                                         :data-toggle   "collapse"
                                         :data-target   "#navbar-collapse"
                                         :aria-expanded "false"}
        [:span.sr-only "Toggle navigation"]
        [:span.icon-bar]
        [:span.icon-bar]
        [:span.icon-bar]]
       [:a.navbar-brand {:title "PASMO | Outreach Database"
                         :href "/"} "PASMO | Outreach Database"]]
      [:div.collapse.navbar-collapse {:id "navbar-collapse"}
       [:ul.nav.navbar-nav
        [:li>a {:href "#"
                :on-click select-create-form}
         [:span.glyphicon.glyphicon-plus {:aria-hidden true}]]]]]]))
