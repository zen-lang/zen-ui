(ns app.symbols
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [stylo.core :refer [c]]
            [app.routes :refer [href]]
            [app.layout :refer [symbol-url url]]
            [clojure.string :as str]))

(defn cls [& xs]
  (->> xs (filter identity) (map name) (str/join " ")))

(zrf/defx ctx
  [{db :db} [_ phase {params :params :as opts}]]
  (cond
    (= :deinit phase) {}

    (= :params phase) {:db (assoc-in db [::view] (:view params))}

    (= :init phase)
    {:db (assoc-in db [::view] (:view params))
     :zen/rpc {:method 'zen-ui/get-symbol
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

(zrf/defx set-view
  [{db :db} [_ v]]
  {:dispatch [:zframes.routing/merge-params {:view (str (:zen/name v))}]})

(defn get-view [db]
  (if-let [v (get-in db [::view])]
    (symbol v)
    'zen-ui/view-for-edn))

(zrf/defs cur-view [db _]
  (get-view db))

(zrf/defs view-data
  [db & _]
  (let [v (get-view db)]
    (get-in db [::db :data :views v])))

(defmulti render-view (fn [{v :view}] (:zen/name v)))


(defmethod render-view 'zen-ui/view-for-tag
  [{d :data}]
  [:div {:class (c [:py 4] :divide-y)}
   (for [{nm :name desc :desc tgs :tags} (->> d (sort-by #(str (:name %))))]
     [:div {:key nm :class (c :flex [:space-x 4] [:py 1])}
      [:a {:href (symbol-url nm) :class (c :block :whitespace-no-wrap [:text :blue-600])}
       (str nm)]
      [:div {:class (c [:text :gray-700] :text-sm)} desc]])])


(declare render-schema)

(defn type-icon [tp]
  [:div {:class
         (str/join " "
                   (mapv name
                         [(c [:w 4] [:h 4] [:mr 1]
                             :border
                             [:bg :gray-200]
                             {:border-radius "4px" :font-size "11px"
                              :border "1px solid #ddd"
                              :line-height "13px"
                              :text-align "center"})
                          (cond
                            (= tp 'zen/set) (c [:text :blue-800])
                            (= tp 'zen/map) (c [:text :green-800])
                            (= tp 'zen/case) (c [:text :red-800])
                            (contains? #{'zen/string 'zen/datetime
                                         'zen/date 'zen/boolean
                                         'zen/keyword 'zen/number} tp)
                            (c [:text :orange-800])
                            :else  (c [:bg :gray-200]))]))}
   (cond
     (= tp 'zen/set) "#"
     (= tp 'zen/map) "{}"
     (= tp 'zen/case) "?"
     :else (first (last (str/split (str tp) #"/"))))])

(defn render-zen-map [sch]
  [:div 
   (when-let [cfs (:confirms sch)]
     [:div "map: "
      (for [cf cfs]
        [:a {:href (symbol-url cf) :class (c [:text :green-700])}
         (str cf)])
      ""])
   (for [[k v] (->> (:keys sch)
                    (sort-by (fn [[_ v]] (:row (meta v)))))]
     [:div {:class (c {:border-left "1px solid #ddd"})}
      [:div {:class (c :flex :items-center)}
       [:div {:class (c :border-b [:w 4])}]
       [:div {:class (c :flex :items-center [:space-x 1])}
        (type-icon (:type v))
        [:div {:class (c [:pr 3] {:font-weight "500"})}
         (if (keyword? k)
           (subs (str k) 1)
           (str k))
         (when (contains? (:require sch) k)
           [:span {:class (c [:ml 1] [:text :red-700])} "*"])]
        (when-let [tp (:type v)]
          [:a {:href (symbol-url tp) :class (c [:text :blue-700])}
           (str tp)])
        (when-let [cfs (:confirms v)]
          [:div (for [cf cfs]
                  [:a {:href (symbol-url cf) :class (c [:text :green-700])}
                   (str cf)])])]]
      (when-let [desc (:zen/desc v)]
        [:div {:class (c :text-xs [:text :gray-600] [:ml 11])}
         (subs desc 0 (min (count desc) 100))])
      (when (not (empty? (dissoc v :confirms :zen/desc)))
        [:div {:class (c [:pl 6])}
         (render-schema ctx (dissoc v :confirms :zen/desc))])])])

(defn render-zen-set [sch]
  #_(when-let [evr (:every sch)]
    [:div [:b "every =>"]
     (render-schema ctx evr)]))

(defn render-zen-vector [sch]
  (when-let [evr (:every sch)]
    [:div {:class (c [:ml 7])}
     [:span "vector "
           (when-let [tp (:type evr)] [:a {:href (symbol-url tp) :class (c [:text :blue-700])} (str tp)])
           (when-let [cfs (:confirms evr)]
             (for [cf cfs]
               [:a {:href (symbol-url cf) :class (c [:text :green-700])}
                (str cf)]))

           " : "]
     [:div {:class (c [:ml 4])}
      (render-schema evr)]]))

(defn render-zen-case [sch]
  #_(when-let [cs (:case sch)]
    [:div
     (for [{w :when th :then} cs]
       [:div
        [:div {:class (c :flex)} [:b "when"] (render-schema ctx w)] 
        (when th
          [:div {:class (c [:ml 2])} [:b "then"] (render-schema ctx th)])])]))

(defn render-schema [sch]
  (cond
    (= 'zen/map (:type sch)) (render-zen-map sch)
    (= 'zen/set (:type sch)) (render-zen-set sch)
    (= 'zen/vector (:type sch)) (render-zen-vector sch)
    (= 'zen/case (:type sch)) (render-zen-case sch)
    (= 'zen/keyword (:type sch)) ""
    (= 'zen/string (:type sch)) ""
    (= 'zen/integer (:type sch)) ""
    (= 'zen/number (:type sch)) ""
    (= 'zen/boolean (:type sch)) ""
    (= 'zen/datetime (:type sch)) ""
    (= 'zen/date (:type sch)) ""
    (nil? (:type sch)) ""
    (= 'zen/symbol (:type sch)) [:div {:class (c :flex [:space-x 1])}
                                 [:a {:href (symbol-url 'zen/symbol)
                                      :class (c [:text :green-700])}
                                  "zen/symbol"]
                                 (when-let [tgs (:tags sch)]
                                   [:div {:class (c :flex [:space-x 1])}
                                    [:div "#{"]
                                    (for [t tgs]
                                      [:a {:class (c [:mr 2] :class (c [:text :blue-700]))
                                           :href (symbol-url t)}
                                       (str t)])
                                    [:div "}"]])]
    (= 'zen/any (:type sch)) ""
    :else (edn sch)))

(defmethod render-view 'zen-ui/view-for-schema
  [{d :data}]
  (render-schema d))

(defmethod render-view :default
  [{d :data}]
  (edn d))

(zrf/defview page [model cur-view view-data]
  [:div {:class (c [:p 8] [:space-y 4])}
   [:h1 {:class (c :text-2xl [:my 2])}
    (str (:zen/name (:model model)))]
   [:p (:zen/desc (:model model))]
   [:div {:class (c :flex :w-full [:mt 4])}
    [:div {:class (c [:w 4] :border-b)}]
    (for [[k {v :view}] (:views model)]
      [:a {:key k
           :class (cls
                   (c :flex-1 [:py 0.5] :border :text-center [:bg :gray-200] [:text :gray-600]
                      {:border-top-left-radius "4px"
                       :border-top-right-radius "4px"})
                   (when (= cur-view (:zen/name v))
                     (c [:text :gray-900]  {:border-bottom-color "transparent" :background-color "white!important" :font-weight "500"})))
           :on-click #(zrf/dispatch [set-view v])}
       (:title v)])
    [:div {:class (c [:w 4] :border-b)}]]
   (render-view view-data)
   ])

(pages/reg-page ctx page)
