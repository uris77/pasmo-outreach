(ns pasmo-outreach.ui.state)

(def app-db {:outreach-list          []
             :loading?               false
             :selected-outreach      {}
             :new-outreach           {:date (js/Date.)}
             :creating-new-outreach? false
             :saving?                false})
