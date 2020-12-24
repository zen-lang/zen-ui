(ns zen-ui.core
  (:require [zen.core :as zen]))



(comment
  (def ctx (zen/new-context))

  ctx

  (zen/load-ns ctx 'demo)

  ctx

  (def srv (web/start {:port 3334} (fn [req] (dispatch ctx req))))


  )
