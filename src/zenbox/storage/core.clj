(ns zenbox.storage.core
  (:require
   [zen.core :as zen]
   [zenbox.rpc :refer [rpc-call]]
   [zenbox.storage.atom :as atom-storage]
   ;; [zenbox.storage.postgres :as postgres-storage]
   [zenbox.storage.zen :as zen-storage]
   [zen.store :as zen-extra]))

(defmulti dispatch-store (fn [ctx rpc resource] (:store rpc)))

(defmethod dispatch-store 'storage/atom
  [ctx rpc params]
  (atom-storage/handle ctx rpc params))

;; (defmethod dispatch-store 'storage/postgres
;;   [ctx rpc params]
;;   (postgres-storage ctx rpc params))

(defmethod dispatch-store 'storage/zen
  [ctx rpc params]
  (zen-storage/handle ctx rpc params))


(defmethod rpc-call 'storage/ensure
  [ctx rpc {params :params}]
  (dispatch-store ctx rpc params))

(defmethod rpc-call 'storage/insert
  [ctx rpc {params :params}]
  (dispatch-store ctx rpc params))

(defmethod rpc-call 'storage/read
  [ctx rpc {params :params}]
  (dispatch-store ctx rpc params))

(defmethod rpc-call 'storage/search
  [ctx rpc {params :params}]
  (dispatch-store ctx rpc params))

(defmethod rpc-call 'storage/delete
  [ctx rpc {params :params}]
  (dispatch-store ctx rpc params))
