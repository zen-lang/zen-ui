(ns zenbox.storage.zen
  (:require [zen.core :as zen]
            [zen.store :as zen-extra]))

(defmulti handle (fn [ctx rpc storage req] (:operation rpc)))

(defmethod handle
  'zenbox/insert
  [ctx rpc storage params]
  (let [{:keys [resource namespace schema]} storage
        name (:zen/name params)
        body (dissoc params :zen/name)
        nmsps (get-in @ctx [:ns (symbol namespace)])
        {:keys [errors] :as val-result} (zen/validate ctx [schema] body)
        ]
    (if (empty? errors)
      {:result (zen-extra/load-symbol ctx nmsps name (merge body resource))}
      {:error errors})))
