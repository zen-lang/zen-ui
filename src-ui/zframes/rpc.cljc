(ns zframes.rpc
  (:require [re-frame.db :as db]
            [cognitect.transit :as t]
            [re-frame.core :as rf]))

(defonce debounce-state (atom {}))

(defn to-transit [x]
  (let [ w (t/writer :json)]
    (t/write w x)))


(defn from-transit [x]
  (let [r (t/reader :json)]
    (t/read r x)))

(defn *rpc-fetch [{:keys [path debounce force success error] :as opts}]
  (println "RPC:" (:method opts) (:params opts))
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
                                :headers {"Accept" "application/transit+json"
                                          "Content-Type" "application/transit+json"
                                          "Cache-Control" "no-cache"}
                                :cache "no-store"
                                :mode "cors"
                                :body (to-transit (select-keys opts [:method :params :id]))}))
            (.then
             (fn [resp]
               (.then (.text resp)
                      (fn [doc]
                        (let [cdoc  (from-transit doc)]
                          (println "PRC:" (:method opts) (:params opts) cdoc)
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
