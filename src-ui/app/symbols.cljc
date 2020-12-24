(ns app.symbols
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [stylo.core :refer [c]]
            [app.routes :refer [href]]
            [app.layout :refer [symbol-url url]]
            [clojure.string :as str]))

(zrf/defx ctx
  [{db :db} [_ phase {params :params :as opts}]]
  (cond
    (= :deinit phase) {}

    (or (= :init phase) (= :params phase))
    {:zen/rpc {:method 'zen-ui/get-symbol
               :params {:name (str/replace (:name opts) #":" "/")}
               :path [::db]}}))

(zrf/defs model [db _]
  (get-in db [::db :data]))

(defn edn [x]
  (cond
    (map? x) [:div {:class (c :flex)}
              [:div {:class (c [:text :gray-500] :bold [:mx 1])} "{"]
              [:div
               (for [[k v] (sort-by (fn [[_ v]] (:row (meta v))) x)]
                 [:div {:class (c :flex [:ml 2]) :key k}
                  (edn k) (edn v)])]]
    (set? x) [:div {:class (c :flex)}
              [:div {:class (c [:text :gray-500] :bold [:mx 1])} "#{"]
              (for [v x]
                [:div {:key v :class (c :flex [:ml 2])} (edn v)])]
    (sequential? x) [:div {:class (c :flex)}
                     [:div {:class (c [:text :gray-500] :bold [:mx 1])} "["]
                     (if (or (keyword? (first x))
                             (symbol? (first x)))
                       [:div {:class (c [:text :green-700] :bold [:mx 1])} (str  (str/join " " x) "]")]
                       [:div
                        (for [[idx v] (map-indexed (fn [i x] [i x]) x)]
                          [:div {:class (c :flex [:ml 2]) :key idx}
                           [:div {:class (c [:text :gray-500] :bold [:mx 1])} (str "[" idx "]")]
                           (edn v)])])]
    (number? x) [:div {:class (c [:text :orange-600])} x]
    (string? x) [:div {:class (c [:text :yellow-700])} (pr-str x)]
    (keyword? x) [:b {:class (c [:mr 2] :font-bold [:text :green-700])
                      :title (pr-str (meta x))}
                  (pr-str x)]
    (symbol? x)  [:a {:class (c [:text :blue-800] [:mr 2]) :href (symbol-url x)}  (pr-str x)]
    :else [:div "?" (pr-str x)]))

(zrf/defview page [model]
  [:div {:class (c [:p 8] [:space-y 4])}
   [:h1 {:class (c :text-xl)}
    (str (:zen/name model))]
   [:div (edn model)]])

(pages/reg-page ctx page)
