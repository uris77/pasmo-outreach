(ns pasmo-outreach.ui.state)

(def app-db {:outreach-list          {:list         []
                                      :current-page 0
                                      :next-page    0
                                      :total        0}
             :loading?               false
             :selected-outreach      {}
             :new-outreach           {:date (js/Date.)}
             :creating-new-outreach? false
             :saving?                false})
