(ns app.dashboard
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [stylo.core :refer [c]]
            [app.routes :refer [href]]
            [clojure.string :as str]))

(zrf/defx ctx
  [{db :db} [_ phase {params :params}]]
  (cond
    (= :deinit phase) {}

    (or (= :init phase) (= :params phase))
    {:zen/rpc {:method 'demo/all-tags
               :path [::model]}}))

(zrf/defs model [db _]
  (get-in db [::model :data]))

(zrf/defview page [model]
  [:div {:class (c [:p 8])}
   "Dashboard"
   [:div {:class (c [:space-y 2] :divide-y)}
    (for [[k vs] model]
      [:div {:key k}
       [:b (str k)]
       [:span (str/join ", " vs)]])]])

(pages/reg-page ctx page)
