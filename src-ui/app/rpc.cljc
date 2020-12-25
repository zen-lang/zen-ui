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
            [clojure.edn]
            [clojure.string :as str]))


(zrf/defs model [db _]
  (get-in db [::model :data]))

(zrf/defx call-rpc
  [{db :db} & _]
  (let [method (get-in db [::db :form :value :method])
        params (clojure.edn/read-string (get-in db [::db :form :value :params]))]
    {:db (assoc-in db [::db :result :error] nil)
     :zen/rpc {:method (symbol method)
               :path [::db :result]
               :params params}}))

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

(zrf/defsp result-loading [::db :result :loading])
(zrf/defsp result [::db :result :data])
(zrf/defsp result-error [::db :result :error])

(zrf/defview page [model result result-error result-loading]
  [:div {:class (c [:p 2])}
   [:div {:class (c [:space-y 3] [:w 200] {:margin "0 auto"})}
    [:h2 {:class (c :text-xl [:py 2] [:my 2] :border-b)}
     "RPC Console"]

    [:div "Method:"]
    [anti.select/zf-select {:placeholder "Method"
                            :opts {:zf/root [::db :form] :zf/path [:method]}}]

    [:div "Params:"]
    [anti.textarea/zf-textarea
     {:opts {:zf/root [::db :form] :zf/path [:params]}}]

    [anti.button/button {:type "primary" :on-click #(zrf/dispatch [call-rpc])} "Send"]

    (when result-loading
      [:div "loading..."])

    (when result-error
      [:div {:class (c [:text :red-500])}
       (if (string? result-error)
         result-error
         (app.symbols/edn result-error))])

    (when (and result (nil? result-error))
      [:div (app.symbols/edn result)])]])

(pages/reg-page ctx page)
