(ns app.rpc
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [zf.core :as zf]
            [stylo.core :refer [c]]
            [anti.select]
            [anti.textarea]
            [anti.button]
            [app.symbols]
            [app.routes :refer [href]]
            [clojure.string :as str]))


(zrf/defs model [db _]
  (get-in db [::model :data]))

(zrf/defx call-rpc
  [{db :db} & _]
  (let [req (get-in db [::db :form :value])]
    {:zen/rpc {:method (:method req)
               :path [::db :result]}}))

(zrf/defx rpc-methods-loaded
  [{db :db} [_ {data :data}]]
  (let [result (map str (:methods data))]
    {:db (assoc-in db
                   (zf/schema-path {:zf/root [::db :form] :zf/path [:method]})
                   {:options result})}))

(zrf/defx ctx
  [{db :db} [_ phase {params :params}]]
  (cond
    (= :deinit phase) {}

    (or (= :init phase) (= :params phase))
    {:zen/rpc {:method 'zen-ui/rpc-methods
               :path [::db :rpc-methods]
               :success {:event rpc-methods-loaded}}}))

(zrf/defsp result [::db :result :data])

(zrf/defview page [model result]
  [:div {:class (c [:p 2])}
   [:div {:class (c [:space-y 3] [:w 200] {:margin "0 auto"})}
    [:h2 "RPC Console"]
    [:div "Method:"]
    [anti.select/zf-select {:placeholder "Method"
                            :opts {:zf/root [::db :form] :zf/path [:method]}}]

    [:div "Params:"]
    [anti.textarea/zf-textarea
     {:opts {:zf/root [::db :form] :zf/path [:params]}}]
    [anti.button/button {:type "primary" :on-click #(zrf/dispatch [call-rpc])} "Send"]

    (when result
      [:div (app.symbols/edn result)])]])

(pages/reg-page ctx page)
