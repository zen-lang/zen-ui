(ns app.symbols
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [stylo.core :refer [c]]
            [anti.button]
            [anti.textarea]
            #?(:cljs  [cljs.pprint])
            #?(:cljs  [cljs.reader])
            [app.routes :refer [href]]
            [app.layout :refer [symbol-url url]]
            [app.monaco]
            [clojure.string :as str]
            [app.errors]
            [clojure.edn]))


(defn cls [& xs]
  (->> xs (filter identity) (map name) (str/join " ")))

(defn pp [x]
  #?(:cljs (with-out-str (cljs.pprint/pprint x))))

(defn read-edn [x]
  #?(:cljs (cljs.reader/read-string x)))

(zrf/defx on-loaded
  [{db :db} [_ {data :data}]]
  {:db (assoc db
              ::editable (pp (dissoc (:model data) :zen/name :zen/file :zen/errors))
              ::edit-mode false)})

(zrf/defx ctx
  [{db :db} [_ phase {params :params :as opts}]]
  (cond
    (= :deinit phase) {:db (dissoc db ::db ::rpc-params ::rpc-result)}

    (= :params phase) {:db (assoc-in db [::view] (:view params))}

    (= :init phase)
    {:db (-> db
             (assoc-in [::view] (:view params))
             (assoc-in [::db :rpc] nil))
     :zen/rpc {:method 'zen-ui/get-symbol
               :params {:name (str/replace (:name opts) #":" "/")}
               :success {:event on-loaded}
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
    (merge
     (get-in db [::db :data :views v])
     {:model (get-in db [::db :data :model])
      :result-loading (get-in db [::db :rpc :result :loading])
      :result (get-in db [::db :rpc :result :data])
      :result-error (get-in db [::db :rpc :result :error])})))

(defmulti render-view (fn [{v :view}] (:zen/name v)))


(defmethod render-view 'zen-ui/view-for-tag
  [{d :data}]
  [:div  {:class (c [:p 4])}
   [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Models tagged:"]
   [:div {:class (c [:py 4] :divide-y)}
    (for [{nm :name desc :desc tgs :tags} (->> d (sort-by #(str (:name %))))]
      [:div {:key nm :class (c :flex [:space-x 4] [:py 1])}
       [:a {:href (symbol-url nm) :class (c :block :whitespace-no-wrap [:text :blue-600])}
        (str nm)]
       [:div {:class (c [:text :gray-700] :text-sm)} desc]])]])


(zrf/defsp validate-query [::validate-query])
(zrf/defsp validate-result [::validate-result])


(zrf/defx call-validate
  [{db :db} & _]
  (let [q (read-edn (get-in db [::validate-query]))
        id (get-in db [::db :data :model :zen/name])]
    {:zen/rpc {:method 'zenbox/validate
               :params {:data (or q {}) :schema id}
               :path [::validate-result]}}))

(zrf/defx validate-query-change
  [{db :db} [_ v]]
  {:db (assoc db ::validate-query v)})

(zrf/defview validate-result-view
  [validate-result]
  [:div
   (when-let [err (:error validate-result)]
     [:div {:class (c [:text :red-500] [:mt 4])}
      [:div
       [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b [:text :red-600])} "Error:"]
       (app.symbols/edn err)]])

   (when-let [res (:data validate-result)]
     [:div
      [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Result:"]
      (app.errors/render-errors (:errors res))])])


(zrf/defview validate-view
  [validate-query]
  [:div
   [:style ".monaco {width: 100%; min-height: 200px; border: 1px solid #ddd;}"]
   [app.monaco/monaco
    {:class (c :block [:w 100] [:h 100])
     :on-change (fn [x] (zrf/dispatch [validate-query-change x]))
     :value (or validate-query "{}")}]

   [anti.button/button {:class (c [:mt 2])
                        :type "primary" :on-click #(zrf/dispatch [call-validate])} "Query"]])

(defmethod render-view 'zen-ui/view-for-validate
  [{d :data}]
  [:div  {:class (c [:p 2])}
   [validate-view]
   [validate-result-view]])


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
  [:div (render-schema d)])

(zrf/defx on-model-change
  [{db :db} [_ value]]
  {:db (assoc db ::editable value)})

(zrf/defx save [{db :db} _]
  (let [val (get db ::editable)
        orig (get-in db [::db :data :model])]
    {:zen/rpc {:method 'zen-ui/update-symbol
               :params  {:data (read-edn val)
                         :name (:zen/name orig)}
               :success {:event on-loaded}
               :path [::db]}}))

(zrf/defsp editor-model [::editable])

(zrf/defview editor [editor-model]
  [:div
   [:style ".monaco {width: 100%; min-height: 200px; border: 1px solid #ddd;}"]
   [app.monaco/monaco
    {:class (c :block [:w 100] [:h 100])
     :on-change (fn [x] (zrf/dispatch [on-model-change x]))
     :value editor-model}]
   ])

(zrf/defsp edit-mode [::edit-mode])
(zrf/defx set-edit-mode
  [{db :db} [_ val]]
  {:db (assoc db ::edit-mode val)})

(zrf/defview edn-edit
  [view-data edit-mode]
  (if edit-mode
    [:div {:class (c [:space-y 4])}
     [editor]
     [:div {:class (c :flex [:space-x 4])}
      [anti.button/button {:type "default" :on-click #(zrf/dispatch [set-edit-mode false])} "Cancel"]
      [anti.button/button {:type "primary" :on-click #(zrf/dispatch [save])} "Save"]]]
    [:div
     (edn (dissoc (:data view-data) :zen/name :zen/file))
     [:br]
     [anti.button/button {:type "default" :on-click #(zrf/dispatch [set-edit-mode true])} "Edit"]]))



(defmethod render-view 'zen-ui/view-for-edn
  [_]
  [:div {:class (c [:p 4])}
   [edn-edit]])

(defmethod render-view 'zen-ui/view-for-api
  [{d :data}]
  [:div
   [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Routes"]
   [:div {:class (c [:p 4] [:space-y 2] :divide-y)}
    (for [r d]
      [:div {:key (:operation r)
             :class (c :flex [:space-x 2])}
       [:div {:class (c {:font-weight "600"})} (name (:method r))]
       [:div {:class (c )} (str "/" (str/join "/" (:path r)))]
       [:div {:class (c [:text :gray-600])}
        [:a {:href (symbol-url (:operation r))}
         (str (:operation r))]]])]

   [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Test routes"]])


(zrf/defx call-rpc
  [{db :db} & _]
  (let [method (get-in db [::db :data :model :zen/name])
        params (clojure.edn/read-string (get-in db [::rpc-params]))]
    {:zen/rpc {:method (symbol method)
               :params params
               :path [::rpc-result]}}))

(zrf/defsp rpc-result [::rpc-result])

(zrf/defx rpc-params-change
  [{db :db} [_ v]]
  {:db (assoc db ::rpc-params v)})

(zrf/defs rpc-params
  [db _]
  (or (get db [::rpc-params])
      (when-let [v (get-in db [::db :data :views 'zen-ui/view-for-rpc :data :placeholder])]
        (pp v))))

(zrf/defview rpc-results-view
  [rpc-result rpc-params]
  [:div
   [:style ".monaco {width: 100%; min-height: 200px; border: 1px solid #ddd;}"]
   [app.monaco/monaco
    {:class (c :block [:w 100] [:h 100])
     :on-change (fn [x] (zrf/dispatch [rpc-params-change x]))
     :value rpc-params}]

   #_[anti.textarea/zf-textarea
      {:opts {:zf/root [::db :rpc :form] :zf/path [:params]}}]

   [anti.button/button {:class (c [:mt 2])
                        :type "primary" :on-click #(zrf/dispatch [call-rpc])} "Send"]
   (when-let [err (:error rpc-result)]
     [:div {:class (c [:text :red-500] [:mt 4])}
      [:div
       [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b [:text :red-600])} "Error:"]
       (app.symbols/edn err)]])

   (when-let [res (:data rpc-result)]
     [:div
      [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Result:"]
      (app.symbols/edn res)])])


(defmethod render-view 'zen-ui/view-for-rpc
  [{model :data}]
  [:div {:class (c [:space-y 2])}
   [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Call '" (str (get model :zen/name))]
   [:div (get model :zen/desc)]

   [rpc-results-view]])

(zrf/defsp pg-query [::pg-query])
(zrf/defsp pg-result [::pg-result])


(def default-sql "
select jsonb_strip_nulls(to_jsonb(t.*)) as tbl
from information_schema.tables t
where table_schema = 'public'
limit 100
")

(zrf/defx call-pg
  [{db :db} & _]
  (let [q (get-in db [::pg-query])
        pg (get-in db [::db :data :model :zen/name])]
    {:zen/rpc {:method 'zenbox/query
               :params {:sql (or q default-sql) :db pg}
               :path [::pg-result]}}))

(zrf/defx pg-query-change
  [{db :db} [_ v]]
  {:db (assoc db ::pg-query v)})

(zrf/defview pg-result-view
  [pg-result]
  [:div
   (when-let [err (:error pg-result)]
     [:div {:class (c [:text :red-500] [:mt 4])}
      [:div
       [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b [:text :red-600])} "Error:"]
       (app.symbols/edn err)]])

   (when-let [res (:data pg-result)]
     [:div
      [:h1 {:class (c :text-xl [:my 2] [:p 1] :border-b)} "Result:"]
      (app.symbols/edn res)])])


(zrf/defview pg-view
  [pg-query]
  [:div
   [:style ".monaco {width: 100%; min-height: 200px; border: 1px solid #ddd;}"]
   [app.monaco/monaco
    {:class (c :block [:w 100] [:h 100])
     :lang "sql"
     :on-change (fn [x] (zrf/dispatch [pg-query-change x]))
     :value (or pg-query default-sql)}]

   [anti.button/button {:class (c [:mt 2])
                        :type "primary" :on-click #(zrf/dispatch [call-pg])} "Query"]])

(defmethod render-view 'zen-ui/view-for-pg
  [{model :data}]
  [:div {:class (c [:space-y 2])}
   [pg-view]
   [pg-result-view]])

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
