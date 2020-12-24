(ns zenbox.storage
  (:require
   [zen.core :as zen]))

(defonce storage (atom {}))

(defmulti inmemory (fn [ctx op params] (:operation op)))
 
(defmethod inmemory 'storage/insert-op
  [ctx op params]
  (let [{:keys [errors]} (zen/validate ctx [(:schema op)] params)]
    (if (not= errors [])
      errors
      (do (swap! storage assoc-in [(:resourceType params) (:id params)] params)
          params))))

(defmethod inmemory 'storage/delete-op
  [ctx op params]
  (swap! storage update (:resourceType params) dissoc (:id params))
  params)

(defmethod inmemory 'storage/read-op
  [ctx op params]
  (get-in @storage [(:resourceType params) (:id params)]))

(defn handle [ctx req]
  (let [params (:params req)
        rpc-config (zen/get-symbol ctx (symbol (:method req)))
        {:keys[persist] :as operation} (zen/get-symbol ctx (:operation rpc-config))]
    (if (= persist 'storage/inmemory)
      (inmemory ctx operation params)
      {:error "not implemented"})))


(comment
  @storage

  )
