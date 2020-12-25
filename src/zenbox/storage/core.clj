(ns zenbox.storage.core
  (:require
   [zen.core :as zen]
   [zenbox.rpc :refer [rpc-call]]
   [zenbox.storage.atom :as atom-storage]
   [zenbox.storage.postgres :as postgres-storage]
   [zenbox.storage.zen :as zen-storage]
   [zen.store :as zen-extra]))

(defmulti dispatch-store (fn [ctx rpc storage resource] (:engine storage)))

(defmethod dispatch-store 'storage/atom
  [ctx rpc storage params]
  (atom-storage/handle ctx rpc storage params))

(defmethod dispatch-store 'storage/jsonb-store
  [ctx rpc storage params]
  (postgres-storage/handle ctx rpc storage params))

(defmethod dispatch-store 'storage/zen
  [ctx rpc storage params]
  (zen-storage/handle ctx rpc storage params))

(defmethod rpc-call 'storage/ensure
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'storage/insert
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'storage/read
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'storage/search
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))

(defmethod rpc-call 'storage/delete
  [ctx rpc {params :params}]
  (let [storage (zen/get-symbol ctx (:storage rpc))]
    (dispatch-store ctx rpc storage params)))
