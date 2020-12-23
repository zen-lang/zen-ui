(ns zen-ui.core
  (:require [zen.core :as zen]
            [zen-ui.web.core :as web]))

(defmulti rpc (fn [ctx req] (symbol (:method req))))

(defmethod rpc 'billogica/rpc-ping
  [_ {params :params}]
  {:result {:message "pong" :params params}})

(defn rpc-call [{zen :zen :as ctx} {id :id meth :method params :params :as rpc-req}]
  (cond-> 
      (let [op-name (and meth (symbol meth))
            op-def (zen/get-symbol zen op-name)]
        (if (contains? (:zen/tags op-def) 'zenbox/rpc)
          (if-let [errs (when-let [prm-schema (:params op-def)]
                          (let [{errs :errors} (zen/validate-schema zen prm-schema params)]
                            (when-not (empty? errs)
                              errs)))]
            {:error {:message "Invalid params" :errors errs}}
            (rpc ctx rpc-req))
          {:error {:message (str "No zenbox/rpc model for " op-name)}}))
    id (assoc :id id)))

(defmethod rpc 'demo/validate [ctx params])

(defn dispatch [ctx {op :resource }]
  (let [res (rpc-call ctx op)]
    (if (:error res)
      {:status 422 :body res}
      {:status 200 :body res})))

(defn start [])
(defn stop [])


(comment
  (def ctx (zen/new-context))

  ctx

  (zen/load-ns ctx 'demo)

  ctx

  (def srv (web/start {:port 3334} (fn [req] (dispatch ctx req))))


  )
