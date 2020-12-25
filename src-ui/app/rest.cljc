(ns app.rest
  (:require [app.pages :as pages]
            [clojure.edn]
            [zframes.re-frame :as zrf]
            [zf.core :as zf]
            [stylo.core :refer [c]]
            [anti.select]
            [anti.textarea]
            [anti.button]
            [app.routes :refer [href]]
            [clojure.string :as str]))

(zrf/defs model [db _]
  (get-in db [::model :data]))

(zrf/defsp endpoints-by-op [::db :endpoints :by-operation])

(defn get-params [uri]
  (str/split "/" uri))

(defn set-params [path params]
  (str/join "/"
            (reduce
             (fn [acc item]
               (if (keyword? item)
                 (conj acc (get params item))
                 (conj acc item)))
             []
             path)))

(zrf/defx call-rest-error
  [{db :db} [_ {data :data}]]
  {:db db})

(zrf/defx call-rest
  [{db :db} & _]
  (let [op (get-in db [::db :form :value :operation])
        endpoint (get-in db [::db :endpoints :by-operation op])
        method (:method endpoint)
        path (:path endpoint)
        params (clojure.edn/read-string (get-in db [::db :form :value :params]))
        uri (set-params path params)]
    {:db db
     :http/fetch {:uri uri
                  :method method
                  :error {:event call-rest-error}}}))

(zrf/defx endpoints-loaded
  [{db :db} [_ {data :data}]]
  (let [options (map
                 (fn [item] (str (:operation item)))
                 (:endpoints data))
        operations (reduce
                    (fn [acc item] (assoc acc (str (:operation item)) item))
                    {}
                    (:endpoints data))]
    {:db (-> db
             (assoc-in
              (zf/schema-path {:zf/root [::db :form] :zf/path [:operation]})
              {:options options})

             (assoc-in [::db :endpoints :by-operation] operations))}))

(zrf/defx ctx
  [{db :db} [_ phase {params :params}]]
  (cond
    (= :deinit phase) {}

    (or (= :init phase) (= :params phase))
    {:zen/rpc {:method 'zen-ui/endpoints
               :path [::db :endpoints]
               :success {:event endpoints-loaded}}}))

(zrf/defview page [model endpoints-by-op]
  [:div {:class (c [:p 2])}
   [:div {:class (c [:space-y 3] [:w 200] {:margin "0 auto"})}
    [:h2 "REST Console"]

    [:div "Endpoint"]
    [anti.select/zf-select {:placeholder "Endpoint"
                            :render-value (fn [op c]
                                            (let [item (get endpoints-by-op op)
                                                  option (str (:method item) " " (:uri item))]
                                              option))

                            :opts {:zf/root [::db :form] :zf/path [:operation]}}]

    [:div "Params:"]
    [anti.textarea/zf-textarea
     {:opts {:zf/root [::db :form] :zf/path [:params]}}]

    [anti.button/button {:type "primary" :on-click #(zrf/dispatch [call-rest])} "Send"]]])



(pages/reg-page ctx page)
