(ns zenbox.storage.atom
  (:require [zen.core :as zen]))

(defmulti handle (fn [ctx rpc storage params] (:operation rpc)))

(defmethod handle
  'storage/insert
  [ctx rpc storage params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema storage)] params)
        path (into [:zen/atom-storage] (:path storage))]
    (if (empty? errors)
      (do (swap! ctx assoc-in (into path [(:resourceType params) (:id params)]) params)
          {:result params})
      {:error errors})))

(defmethod handle
  'storage/delete
  [ctx rpc storage params]
  (let [path (into [:zen/atom-storage] (:path storage))]
    (swap! ctx update-in (into path [(:resourceType params)]) dissoc (:id params))
    {:result params}))

(defmethod handle
  'storage/read
  [ctx rpc storage params]
  (let [path (into [:zen/atom-storage] (:path storage))]
    {:result (get-in @ctx (into path [(:resourceType params) (:id params)]))}))
