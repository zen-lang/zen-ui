(ns zenbox.storage.atom
  (:require [zen.core :as zen]))

(defmulti handle (fn [ctx rpc req] (:operation rpc)))

(defmethod handle
  'storage/insert
  [ctx rpc params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema rpc)] params)
        path (into [:zen/atom-storage] (:path rpc))]
    (if (empty? errors)
      (do (swap! ctx assoc-in (into path [(:resourceType params) (:id params)]) params)
          {:result params})
      {:error errors})))

(defmethod handle
  'storage/delete
  [ctx rpc params]
  (let [resource (handle
                  ctx (assoc rpc :operation 'storage/read) params)
        path (into [:zen/atom-storage] (:path rpc))]
    (if (:error resource)
      resource
      (if (nil? (:result resource))
        {:error [{:message "resource doesn't exists"}]}
        (do
          (swap! ctx update-in (into path [(:resourceType params)]) dissoc (:id params))
          {:result params})))))

(defmethod handle
  'storage/read
  [ctx rpc params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema rpc)] params)
        path (into [:zen/atom-storage] (:path rpc))]
    (if (empty? errors)
      {:result (get-in @ctx (into path [(:resourceType params) (:id params)]))}
      {:error errors})))
