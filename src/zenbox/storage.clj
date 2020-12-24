(ns zenbox.storage
  (:require
   [zen.core :as zen]))

(defonce storage (atom {}))


(defmulti inmemory (fn [ctx op params] op))

(defmethod inmemory 'storage/insert-op
  [ctx op params]
  (swap! storage assoc-in [(:resourceType params) (:id params)] params)
  params)

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
        {:keys[persist operation] :as rest} (zen/get-symbol ctx (:operation rpc-config))]
    (if (= persist 'storage/inmemory)
      (inmemory ctx operation params)
      {:error "not implemented"})))


(comment
  @storage

  )
