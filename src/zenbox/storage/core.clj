(ns zenbox.storage.core
  (:require
   [zen.core :as zen]
   [zenbox.rpc :refer [rpc-call]]
   [zenbox.storage.atom :as atom-storage]
   [zenbox.storage.postgres :as postgres-storage]))

(defmulti dispatch-store (fn [ctx rpc storage resource] (:engine storage)))

(defmethod dispatch-store 'zenbox/atom-store
  [ctx rpc storage params]
  (atom-storage/handle ctx rpc storage params))

(defmethod dispatch-store 'zenbox/jsonb-store
  [ctx rpc storage params]
  (postgres-storage/handle ctx rpc storage params))

(defmethod rpc-call 'zenbox/ensure
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))


(defmethod rpc-call 'zenbox/insert
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (println "Insert" storage params)
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'zenbox/read
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'zenbox/search
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'zenbox/delete
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))
