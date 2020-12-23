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
   {:href  "#/users"
    :icon  "fa-user"
    :title "Users"}])

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


(defn layout [content]
  [:div {:class (c :flex :items-stretch :h-screen)}
   [:style "body {padding: 0; margin: 0;}"]
   [quick-menu]
   [:div {:class (c :flex-1 :overflow-y-auto)} content]])
