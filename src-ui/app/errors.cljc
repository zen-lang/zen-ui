(ns app.errors
  (:require [app.pages :as pages]
            [zframes.re-frame :as zrf]
            [stylo.core :refer [c]]
            [app.routes :refer [href]]
            [app.layout]
            [clojure.string :as str]))

(zrf/defx ctx
  [{db :db} [_ phase {params :params}]]
  (cond
    (= :deinit phase) {}

    (or (= :init phase) (= :params phase))
    {:zen/rpc {:method 'zen-ui/errors
               :path [::model]}}))

(zrf/defs model [db]
  (let [errs (get-in db [::model :data :errors])]
    (->> (group-by :resource errs)
         (sort-by first))))

(zrf/defview page [model]
  [:div {:class (c [:p 8])}
   [:div {:class (c :text-xl [:py 1] [:my 2] :border-b)}
    "Errors"]
   [:div {:class (c [:space-y 2] :divide-y)}
    (for [[res errs] model]
      [:div {:class (c [:mt 4])}
       (if res
         [:a {:href (app.layout/symbol-url res) :class (c :block :text-xl [:text :blue-600] :bold [:py 2] :border-b)}
          (str res)]
         [:b "Global"])
       [:div {:class (c [:space-y 1] :divide-y)}
        (for [err errs]
          [:div {:key (:resource err) :class (c :flex [:space-x 3])}
           (when-let [tp (:type err)]
             [:div {:class (c [:w 40] [:text :gray-500] {:font-weight 400})} tp])
           [:div
            [:div (:message err)]
            [:div
             (when (:path err)
               [:div
                [:b "in: "]
                (pr-str (:path err))])
             (when (:schema err)
               [:div
                [:b "by: "]
                (pr-str (:schema err))])]]
           
           ])]])]])

(pages/reg-page ctx page)
