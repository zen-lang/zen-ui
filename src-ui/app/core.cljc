(ns app.core
  (:require #?(:cljs [reagent.dom])
            [re-frame.core :as rf]
            [zframes.re-frame :as zrf]
            [zframes.cookies]
            [zframes.routing :refer [route not-found?]]
            [zframes.debounce]
            [zframes.window]
            [zframes.console]
            [zframes.storage]
            #?(:cljs [zframes.http])
            [zframes.rpc]
            [zframes.hotkeys]
            [zf.core]
            [app.routes :as routes]
            [app.layout]
            [app.pages :as pages]
            [app.anti]
            [app.dashboard]
            [app.rpc]
            [app.symbols]
            [app.rest]
            #?(:cljs [app.reagent])
            [app.reframe]))


(zrf/defview current-page
  [route not-found?]
  [app.layout/layout
   [:div (if not-found?
           [:div.not-found (str "Route not found")]
           (if-let [page (get @pages/pages (:match route))]
             [page (:params route)]
             [:div.not-found (str "Page not defined [" (:match route) "]")]))]])


(zrf/defx initialize
  [fx _]
  (println "INIT"))

(zrf/defx ctx
  [{db :db} [_ phase {params :params}]]
  (println "CTX nav:")
  (cond
    (= :deinit phase) {}

    (or (= :init phase) (= :params phase))
    {:zen/rpc {:method 'zen-ui/navigation
               :path [:navigation]}}))

(defn mount-root []
  (rf/clear-subscription-cache!)
  #?(:cljs
     (reagent.dom/render
      [current-page]
      (.getElementById js/document "app"))))

(defn init! []
  (zframes.routing/init routes/routes routes/global-contexts)
  (zrf/dispatch [initialize])
  (mount-root))
