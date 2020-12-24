(ns app.layout
  (:require [zframes.re-frame :as zrf]
            [stylo.core :refer [c]]
            [clojure.string :as str]))

(zrf/defs current-uri [db]
 (:uri (first (:route/history db))))

(def quicks
  [{:href  "#/"
    :icon  "fa-tachometer"
    :title "Dashbrd"}
   {:href  "#/rpc"
    :icon  "fa-terminal"
    :title "RPC"}])

(zrf/defx logout [_ _]
  {:cookie/remove "asid"
   :zframes.redirect/page-redirect {:uri "/auth/login"}})

(zrf/defx do-toggle-menu
  [{db :db} [_ state]]
  {:db (if (nil? state)
         (update db ::menu #(not %))
         (assoc db ::menu state))})

(zrf/defs toggle-menu
  [db _]
  (get db ::menu))

(def main-items
  [{:title "Biller Workflow"
    :items [{:href "#/" :title "Dashboard"}
            {:href "#/users" :title "Users"}]}
   {:title "Master Data"
    :items []}])


(defn main-menu []
  [:div {:class (c :absolute :flex [:space-x 8] [:bg :white] [:inset 0] [:left 14] [:z 1001] [:p 10])}
   (for [{t :title xs :items} main-items]
     [:div {:key t :class (c :flex :flex-col [:space-y 1])}
      [:h3 {:class (c :text-bold [:mb 2] {:border-bottom "1px solid #ddd"})} t]
      (for [x xs]
        [:a {:href     (:href x)
             :class    (c {:opacity "0.8"} [:hover {:opacity "1"}])
             :target   (:target x)
             :key      (:href x)
             :on-click #(zrf/dispatch [do-toggle-menu false])}
         (:title x)])])])

(defn quick-item
  [item]
  [:a {:href     (:href item)
       :target   (:target item)
       :on-click (:on-click item)
       :class    [(c [:w 14] [:h 14] :flex :flex-col :justify-center :items-center [:text :white]
                     [:opacity 60] {:border-left "2px solid transparent"} [:flex-shrink 0]
                     [:hover [:text :white] [:opacity 100]])
                  (when (:active item)
                    (c [:opacity 100] {:border-left "2px solid #ffd23f"}))
                  (:class item)]}
   [:i.fal {:class [(c [:my 1] :text-xl) (:icon item)]}]
   [:span {:class (c {:font-size "9px"})} (:title item)]])

(zrf/defview quick-menu [toggle-menu current-uri]
  [:<>
   (when toggle-menu [main-menu])
   [:div {:class (c [:z 100] :overflow-hidden :flex :flex-col
                    {:background-color "#2c3645"
                     :box-shadow       "4px 0 6px -1px rgba(0, 0, 0, 0.15), 2px 0 4px -1px rgba(0, 0, 0, 0.09)"})}
    [quick-item
     {:title    "Menu"
      :icon     "fa-bars"
      :active   toggle-menu
      :on-click #(zrf/dispatch [do-toggle-menu])}]
    (for [i quicks]
      ^{:key (or (:key i) (:href i))}
      [quick-item
       (-> i
           (assoc :active (and (not toggle-menu) (= current-uri (:href i))))
           (update :on-click (fn [f] (comp #(zrf/dispatch [::toggle-menu false])
                                           (or f identity)))))])
    [:div {:class (c :flex-1)}]
    [quick-item
     {:key   "logout"
      :on-click #(zrf/dispatch [logout])
      :icon     "fa-sign-out-alt"
      :title    "Logout"}]]])


(zrf/defsp nav-model [:navigation :data])

(defn url [pth & [ext]]
  (str "#/" (str/join "/" pth) (when ext (str "." (name ext)))))

(defn symbol-url [sym]
  (url ["symbols" (str/replace sym #"/" ":")]))

(defn symbol-icon [v]
  (let [tgs (into #{} (:tags v))]
    [:div {:class
           (str/join " "
                     (mapv name
                           [(c [:w 3] [:h 3] [:mr 1]
                               {:border-radius "100%" :font-size "9px" :text-align "center" :line-height "0.75rem"})
                            (cond
                              (contains? tgs "zen/type")     (c  [:bg :green-400])
                              (contains? tgs "zen/tag")      (c  [:bg :orange-300])
                              (contains? tgs "zen/property") (c  [:bg :blue-300])
                              (contains? tgs "zen/valueset") (c  [:bg :pink-300])
                              (contains? tgs "zen/schema")   (c  [:bg :green-300])
                              :else                         (c :border [:bg :gray-300]))]))
           :title (str/join " " tgs)}
     (cond
         (contains? tgs "zen/tag") "#"
         (contains? tgs "zen/type")  "T"
         (contains? tgs "zen/valueset")  "V"
         (contains? tgs "zen/schema") "S")]))

(defn render-tree [syms]
  [:div {:class (c [:pl 2])}
   (for [[k v] (sort-by first syms)]
     [:div {:key k}
      [:a {:href (when-let [nm (:name v)] (symbol-url nm))
             :class (c :block :flex :items-baseline :align-baseline [:py 0.25] [:text :gray-700]
                       [:hover [:text :gray-900]])}
       (when-let [tgs (:tags v)]
         (symbol-icon v))
       [:div (subs (str k) 1)]]
      (when-let [ch (and (:children v))]
        [:div {:class (c )}
         (render-tree ch)])])])

(zrf/defview navigation [nav-model]
  [:div {:class (c {})}
   (render-tree nav-model)])

(defn layout [content]
  [:div {:class (c :flex :items-stretch :h-screen)}
   [:style "body {padding: 0; margin: 0;}"]
   [quick-menu]
   [:div {:class (c :flex-1 :flex :overflow-y-auto)}
    [:div {:class (c [:px 4] [:py 2] [:w 80] [:w-min 80]
                     :text-sm [:text :gray-700] [:bg :gray-100]
                     :overflow-y-auto)}
     [:div {:class (c [:pl 2])}
      [navigation]]]
    [:div content]]])
