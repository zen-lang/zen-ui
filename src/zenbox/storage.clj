(ns zenbox.storage
  (:require
   [zen.core :as zen]
   [zen.store :as zen-extra]))

(defonce storage (atom {}))

(defmulti handle-with-atom
  (fn [ctx op params] (:operation op)))

(defmethod handle-with-atom
   'storage/insert-op
  [ctx op params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema op)] params)]
    (if (not= errors [])
      {:error errors}
      (do (swap! storage assoc-in [(:resourceType params) (:id params)] params)
          {:result params}))))

(defmethod handle-with-atom
   'storage/delete-op
  [ctx op params]
  (let [resource (handle-with-atom
   ctx (assoc op :operation 'storage/read-op) params)]
    (if (:error resource)
      resource
      (if (nil? (:result resource))
      {:error [{:message "resource doesn't exists"}]}
      (do
        (swap! storage update (:resourceType params) dissoc (:id params))
        {:result params})))))

(defmethod handle-with-atom
   'storage/read-op
  [ctx op params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema op)] params)]
    (if (not= errors [])
      {:error errors}
      {:result (get-in @storage [(:resourceType params) (:id params)])})))

(defn handle-with-zen [ctx operation params]
  (if (= (:operation operation) 'storage/insert-op)
    (let [{:keys [tags namespace]} (zen/get-symbol ctx (:storage operation))
          name (:zen/name params)
          resource (dissoc params :zen/name)
          nmsps (get-in @ctx [:ns (symbol namespace)])
          ;; {:keys [errors]} (zen/validate ctx [(:schema operation)] resource)
          errors []
          ]
      (if (not= errors [])
        {:error errors}
        {:result (zen-extra/load-symbol ctx nmsps name (assoc resource :zen/tags tags))}))
    {:error "not implemented!!!"}))

(defn handle [ctx rpc params]
  (let [{:keys[storage] :as operation} (zen/get-symbol ctx (:handler rpc))
        {:keys [persist]} (zen/get-symbol ctx storage)
        {:keys [kind]} (zen/get-symbol ctx persist)]
    (case kind
      "atom" (handle-with-atom ctx operation params)
      "zen" (handle-with-zen ctx operation params)
      {:error "not implemented!!!"})))

(comment
  @storage

  )
