(ns zenbox.storage.zen
  (:require [zen.core :as zen]
            [zen.store :as zen-extra]))

(defmulti handle (fn [ctx rpc req] (:operation rpc)))

(defmethod handle
  'storage/insert
  [ctx rpc params]
  (let [{:keys [tags namespace schema]} rpc
        name (:zen/name params)
        resource (dissoc params :zen/name)
        nmsps (get-in @ctx [:ns (symbol namespace)])
        {:keys [errors] :as val-result} (zen/validate ctx [schema] resource)
        ]
    (if (empty? errors)
      {:result (zen-extra/load-symbol ctx nmsps name (assoc resource :zen/tags tags))}
      {:error errors})))
