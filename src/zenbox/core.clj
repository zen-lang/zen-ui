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
  [ctx {{nm :name} :params}]
  (let [model (zen/get-symbol ctx (symbol nm))
        views (resolve-views ctx  model)]
    {:result {:views views :model model}}))

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
  {:result (storage/handle ctx req)})

(defmethod rpc-call 'demo/read-patient
  [ctx req]
  {:result (storage/handle ctx req)})

(defmethod rpc-call 'demo/delete-patient
  [ctx req]
  {:result (storage/handle ctx req)})

(defmethod rpc-call 'zen-ui/rpc-methods
  [ctx req]
  {:result {:methods (zen/get-tag ctx 'zenbox/rpc)}})

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
  


  )


