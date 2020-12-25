(ns zenbox.storage.postgres)

(defmulti handle (fn [ctx rpc storage params] (:operation rpc)))
