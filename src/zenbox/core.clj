(ns zenbox.core
  (:require
   [zenbox.web.core :as web]
   [zen.core :as zen]))

(defmulti rest-op (fn [ctx op req] (:operation (:match op))))

(defmethod rest-op 'demo/rpc
  [ctx op req]
  )

(defn dispatch-op [ctx op request]
  (if op 
    (rest-op ctx op request)
    {:status 404}))

(defn start [ctx]
  (web/start ctx dispatch-op))

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





  )


