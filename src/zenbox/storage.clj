(ns zenbox.storage
  (:require
   [zen.core :as zen]))

(defonce storage (atom {}))

(defmulti inmemory (fn [ctx op params] (:operation op)))

(defmethod inmemory 'storage/insert-op
  [ctx op params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema op)] params)]
    (if (not= errors [])
      {:error errors}
      (do (swap! storage assoc-in [(:resourceType params) (:id params)] params)
          {:result params}))))

(defmethod inmemory 'storage/delete-op
  [ctx op params]
  (let [resource (inmemory ctx (assoc op :operation 'storage/read-op) params)]
    (if (:error resource)
      resource
      (if (nil? (:result resource))
      {:error [{:message "resource doesn't exists"}]}
      (do
        (swap! storage update (:resourceType params) dissoc (:id params))
        {:result params})))))

(defmethod inmemory 'storage/read-op
  [ctx op params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema op)] params)]
    (if (not= errors [])
      {:error errors}
      {:result (get-in @storage [(:resourceType params) (:id params)])})))

(defn handle [ctx rpc params]
  (let [{:keys[storage] :as operation} (zen/get-symbol ctx (:handler rpc))
        {:keys [persist]} (zen/get-symbol ctx storage)
        {:keys [kind]} (zen/get-symbol ctx persist)]
    (if (= kind "atom")
      (inmemory ctx operation params)
      {:error "not implemented!!!"})))

(comment
  @storage

  )
