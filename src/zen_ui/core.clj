(ns zen-ui.core
  (:require [zen.core :as zen]
            [zen-ui.web.core :as web]))

(defn dispatch [req]
  {:status 200 :body {}})

(defn start [])
(defn stop [])


(comment
  (web/start {:port 3333} #'dispatch)


  )
