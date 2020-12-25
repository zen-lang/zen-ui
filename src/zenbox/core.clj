(ns zenbox.core
  (:require
   [zenbox.web.core :as web]
   [zen.core :as zen]
   [zenbox.storage :as storage]
   [zenbox.pg.core]
   [clojure.string :as str]))

(defmulti operation (fn [ctx op req] (:operation op)))
(defmulti rpc-call (fn [ctx rpc req] (or (:operation rpc) (:zen/name rpc))))

(defmethod operation 'zenbox/json-rpc
  [ctx op req]
  (let [resource (:resource req)
        method (:method resource)
        rpc (zen/get-symbol ctx (symbol method))
        resp (rpc-call ctx rpc resource)]
    (if (:result resp)
      {:status 200 :body resp}
      {:status 422 :body resp})))

(defmethod operation 'zenbox/response
  [ctx op req]
  (:response op))

(defmethod rpc-call 'demo/dashboard
  [ctx rpc req]
  {:result {:message "Dashboard"}})

(defmethod rpc-call 'demo/all-tags
  [ctx rpc req]
  {:result (:tags @ctx)})

(defmulti view (fn [ctx view model] (:zen/name view)))

(defmethod view 'zen-ui/view-for-schema
  [ctx view model]
  model)

(defmethod view 'zen-ui/view-for-tag
  [ctx view model]
  (let [tag (:zen/name model)]
    (->> (zen/get-tag ctx tag)
         (mapv (fn [x]
                 (let [m (zen/get-symbol ctx x)]
                   {:name x :desc (:zen/desc m) :tags (:zen/tags m)}))))))

(defmethod view 'zen-ui/view-for-valuset
  [ctx view model]
  model)

(defmethod view 'zen-ui/view-for-edn
  [ctx view model]
  model)

(defmethod view :default [ctx view model]
  {:status :error
   :message (str "No impl for " (:zen/name view))})

(defn resolve-views [ctx model]
  (let [tags (:zen/tags model)]
    (->> (zen/get-tag ctx 'zen-ui/tag-view)
         (reduce (fn [acc tv]
                   (let [v (zen/get-symbol ctx tv)]
                     (if (or (nil? (:tag v)) (contains? tags (:tag v)))
                       (assoc acc tv {:view v :data (view ctx v model)})
                       acc)
                     )) {}))))

(defmethod rpc-call 'zen-ui/get-symbol
  [ctx rpc {{nm :name} :params}]
  (let [model (zen/get-symbol ctx (symbol nm))
        views (resolve-views ctx  model)]
    {:result {:views views :model model}}))

(defmethod rpc-call 'zen-ui/navigation
  [ctx rpc req]
  (let [symbols (->>
                 (:symbols @ctx)
                 (sort-by first)
                 (reduce (fn [acc [nm data]]
                           (let [pth (interpose :children (str/split (str nm) #"[./]"))]
                             (assoc-in acc pth {:name nm :path pth :tags (:zen/tags data) :desc (:zen/desc data)})))
                         {}))
        tags (zen/get-tag ctx 'zen/tag)]
    {:result {:symbols symbols :tags tags}}))

(defmethod rpc-call 'demo/insert-patient
  [ctx rpc req]
  {:result (storage/handle ctx req)})

(defmethod rpc-call 'demo/read-patient
  [ctx rpc req]
  {:result (storage/handle ctx req)})

(defmethod rpc-call 'storage/handle
  [ctx rpc req]
  (storage/handle ctx rpc (:params req)))

(defmethod rpc-call 'zen-ui/rpc-methods
  [ctx rpc req]
  {:result {:methods (zen/get-tag ctx 'zenbox/rpc)}})

(defmethod rpc-call 'zen-ui/endpoints
  [ctx rpc req]
  {:result {:endpoints (zen/get-tag ctx 'zenbox/api)}})

(defmulti create-store (fn [ctx store] (:engine store)))

(defn table-ddl [tbl]
  (format "
  CREATE TABLE IF NOT EXISTS \"%s\" (
    id serial primary key,
    ts timestamptz DEFAULT current_timestamp,
    resource jsonb
  ); " tbl))

(defmethod create-store 'zenbox/jsonb-store
  [ctx {tbl :table-name db-nm :db}]
  (if-let [db (get-in @ctx [:services db-nm])]
    {:result (zenbox.pg.core/exec! db (table-ddl tbl))}
    {:error (str "No connection to " db-nm)}))

(defmethod rpc-call 'zenbox/sql
  [ctx {db-nm :db} {q :query}]
  (if-let [db (get-in @ctx [:services db-nm])]
    {:result (zenbox.pg.core/query db q)}
    {:error {:message (str "No connection to " db-nm)}}))

(defmethod rpc-call 'zenbox/ensure-stores
  [ctx rpc req]
  (let [dbs (:dbs rpc)
        stores (->> (zen/get-tag ctx 'zenbox/store)
                    (mapv (fn [s] (zen/get-symbol ctx s)))
                    (mapv (fn [store]
                            (when (contains? dbs (:db store))
                              (create-store ctx store)))))]
    {:result {:dbs dbs :stores stores}}))

(defn rpc [ctx req]
  (if-let [op (zen/get-symbol ctx (:method req))]
    (rpc-call ctx op (:params req))
    {:error {:message (str "No operation defined for " (:method req))}}))

(defn dispatch-op [ctx route request]
  (if route
    (if-let [op (zen/get-symbol ctx (get-in route [:match :operation]))]
      (operation ctx op request)
      {:status 404})
    {:status 404}))

(defn start [ctx]
  (web/start ctx #'dispatch-op))

(defn stop [ctx]
  (web/stop ctx))

(comment
  (def ctx (zen/new-context))

  (zen/read-ns ctx 'demo)

  (start ctx)

  (stop ctx)

  (:zenbox/servers @ctx)

  (->>
   (zen/get-tag ctx 'zenbox/server)
   (mapv (fn [sym] (zen/get-symbol ctx sym))))

  (resolve-views ctx #{'zen/schema})
  
  (operation
   ctx
   {:operation 'zenbox/json-rpc}
   {:resource
    {:method 'demo/create-pgstore  :params {:zen/name 'click-house
                                            :kind "postgres"
                                            :user "superadmin"
                                            :password "123"
                                            :host "clickhouse-db"
                                            :port 5432}}})


  )


