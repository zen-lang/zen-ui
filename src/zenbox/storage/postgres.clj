(ns zenbox.storage.postgres
  (:require [zenbox.rpc :refer [rpc-call]]
            [zenbox.pg.core]
            [zen.core :as zen]))

(defmulti handle (fn [ctx rpc storage params] (:operation rpc)))


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

