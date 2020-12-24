(ns zenbox.core
  (:require
   [zenbox.web.core :as web]
   [zen.core :as zen]))

(defmulti operation (fn [ctx op req] (:operation op)))
(defmulti rpc-call (fn [ctx req] (symbol (:method req))))

(defmethod operation 'zenbox/json-rpc
  [ctx op req]
  (let [resp (rpc-call ctx (:resource req))]
    (if (:result resp)
      {:status 200 :body resp}
      {:status 422 :body resp})))

(defmethod rpc-call 'demo/dashboard
  [ctx req]
  {:result {:message "Dashboard"}})

(defmethod rpc-call 'demo/all-tags
  [ctx req]
  {:result (:tags @ctx)})

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





  )


