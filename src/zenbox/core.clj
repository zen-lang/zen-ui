(ns zenbox.core
  (:require
   [zenbox.web.core :as web]
   [zen.core :as zen]
   [zenbox.storage :as storage]
   [clojure.string :as str]))

(defmulti operation (fn [ctx op req] (:operation op)))
(defmulti rpc-call (fn [ctx req] (symbol (:method req))))

(defmethod operation 'zenbox/json-rpc
  [ctx op req]
  (let [resp (rpc-call ctx (:resource req))]
    (if (:result resp)
      {:status 200 :body resp}
      {:status 422 :body resp})))

(defmethod operation 'zenbox/response
  [ctx op req]
  (:response op))

(defmethod rpc-call 'demo/dashboard
  [ctx req]
  {:result {:message "Dashboard"}})

(defmethod rpc-call 'demo/all-tags
  [ctx req]
  {:result (:tags @ctx)})

(defmethod rpc-call 'zen-ui/get-symbol
  [ctx {{nm :name} :params}]
  {:result (zen/get-symbol ctx (symbol nm))})

(defmethod rpc-call 'zen-ui/navigation
  [ctx req]
  (let [symbols (->>
                 (:symbols @ctx)
                 (sort-by first)
                 (reduce (fn [acc [nm data]]
                           (let [pth (interpose :children (str/split (str nm) #"[./]"))]
                             (assoc-in acc pth {:name nm :path pth :tags (:zen/tags data) :desc (:zen/desc data)})))
                         {}))]
    {:result symbols}))

(defmethod rpc-call 'demo/insert-patient
  [ctx req]
  {:result (storage/insert ctx req)})

(defmethod rpc-call 'demo/delete-patient
  [ctx req]
  {:result (storage/delete ctx req)})

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


