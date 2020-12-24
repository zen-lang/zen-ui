(ns app.rest
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [zf.core :as zf]
            [stylo.core :refer [c]]
            [anti.select]
            [anti.textarea]
            [anti.button]
            [app.routes :refer [href]]
            [clojure.string :as str]))


(zrf/defx ctx
  [{db :db} [_ phase {params :params}]]
  )


(zrf/defview page []
  [:div {:class (c [:p 2])}
   [:div {:class (c [:space-y 3] [:w 200] {:margin "0 auto"})}
    [:h2 "REST Console"]

    ]])

(pages/reg-page ctx page)
