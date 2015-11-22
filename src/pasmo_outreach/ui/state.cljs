(ns pasmo-outreach.ui.state)

(def app-db {:outreach-list          {:list         []
                                      :current-page -1
                                      :next-page    0
                                      :total        0}
             :page-size              5
             :loading?               false
             :selected-outreach      {}
             :new-outreach           {:date (js/Date.)}
             :creating-new-outreach? false
             :saving?                false})
