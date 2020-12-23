(ns zframes.rpc
  (:require [re-frame.db :as db]
            [re-frame.core :as rf]))

(defonce debounce-state (atom {}))

(defn *rpc-fetch [{:keys [path debounce force success error] :as opts}]
  #?(:cljs
    (if (and debounce path (not force))
      (do
        (when-let [t (get @debounce-state path)]
          (js/clearTimeout t))
        (swap! debounce-state assoc path (js/setTimeout #(*rpc-fetch (assoc opts :force true)) debounce)))
      (let [db db/app-db
            dispatch-event (fn [event payload]
                             (when (:event event)
                               (rf/dispatch [(:event event) (merge event {:request opts} payload)])))]

        (when path
          (swap! db assoc-in (conj path :loading) true))

        (-> (js/fetch "/json-rpc"
                      (clj->js {:method "post"
                                :headers {"accept" "application/json"
                                          "Content-Type" "application/json"
                                          "Cache-Control" "no-cache"}
                                :cache "no-store"
                                :mode "cors"
                                :body (.stringify js/JSON (clj->js (select-keys opts [:method :params :id])))}))
            (.then
             (fn [resp]
               (.then (.json resp)
                      (fn [doc]
                        (let [cdoc (js->clj doc :keywordize-keys true)]
                          (if-let [res  (and (< (.-status resp) 299) (:result cdoc))]
                            (do (swap! db update-in path merge {:loading false :data res})
                                (when success (dispatch-event success {:response resp :data res})))
                            (do
                              (swap! db update-in path merge {:loading false :error (or (:error cdoc) cdoc)})
                              (when error (dispatch-event error {:response resp :data (or (:error cdoc) cdoc)})))))))))
            (.catch (fn [err]
                      (.error js/console err)
                      (swap! db update-in path merge {:loading false :error {:err err}})
                      (when error (dispatch-event error {:error err})))))))))

(defn rpc-fetch [opts]
  (if (vector? opts)
    (doseq [o opts] (when (some? o) (*rpc-fetch o)))
    (*rpc-fetch opts)))

(rf/reg-fx :zen/rpc rpc-fetch)
