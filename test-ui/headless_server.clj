(ns headless-server
  (:require [mybox.world :as world]
            ([re-frame.core :as rf])
            [re-frame.interop]
            [app.core]
            [anti.select-model]
            [anti.radio-model]
            [re-frame.db]
            [re-frame.router]
            [matcho.core :as matcho]
            [re-test]
            [route-map.core :as route-map]
            [app.routes]
            [zf.core :as zf]
            [klog.core]
            [clojure.walk :refer [postwalk]]
            [clojure.string :as str]))


(def app-db re-test/app-db)

(defn reset-db []
  (reset! re-frame.db/app-db {}))

(defn json-fetch [{:keys [uri token headers is-fetching-path params success error] :as opts}]
  (if (vector? opts)
    (doseq [o opts] (json-fetch o))
    (let [headers (cond-> {"accept" "application/json"}
                    token (assoc "authorization" (str "Bearer " token))
                    (nil? (:files opts)) (assoc "Content-Type" "application/json")
                    true (merge (or headers {})))
          request (-> opts
                      (dissoc :method)
                      (dissoc :body)
                      (assoc :resource (:body opts))
                      (assoc :headers headers)
                      (assoc :query-string (str/join "&" (map (fn [[k v]] (str (name k) "=" v)) (:params opts)))) ;; FIXME: Probably duplicate
                      (assoc :request-method
                             (if-let [m (:method opts)]
                               (keyword (str/lower-case (name m)))
                               :get)))
          resp (world/dispatch request)]
      (if (< (:status resp) 299)
        (when-let [ev (:event success)]
          (rf/dispatch [ev (merge success {:request opts :response resp :data (:body resp)})]))
        (when-let [ev (:event error)]
          (rf/dispatch [ev (merge error {:request opts :response resp :data (:body resp)})]))))))

(def browser (atom {}))

(defn do-unbundle [data-key map-f resp]
  (if (or (:entry resp) (= "Bundle" (:resourceType resp)))
    (let [data (mapv (if map-f (fn [x] (map-f (:resource x))) :resource) (:entry resp))]
      {data-key data
       :total (:total resp)})
    {data-key resp}))

(defn *http-fetch [{:keys [method uri headers params path as map-f process unbundle success error] :as opts}]
  (klog.core/log :rf/fetch (select-keys opts [:uri :method :params]))
  (let [db re-frame.db/app-db
        data-key (or as :data)
        request (-> opts
                    (dissoc :method)
                    (dissoc :body)
                    (assoc :resource (:body opts))
                    (assoc :headers headers)
                    (assoc :query-string (str/join "&" (map (fn [[k v]] (str (name k) "=" v)) (:params opts)))) ;; FIXME: Probably duplicate
                    (assoc :request-method
                           (if-let [m (:method opts)]
                             (keyword (str/lower-case (name m)))
                             :get)))
        dispatch-event (fn [event payload]
                         (when (:event event)
                           (rf/dispatch [(:event event) (merge event {:request opts} payload)])))
        resp (world/dispatch request)
        doc (:body resp)]
    (if (< (:status resp) 300)
      (klog.core/log :rf/fetch (select-keys resp [:status]))
      (klog.core/log :rf/fetch (assoc (select-keys resp [:status :body]) :lvl :error)))
    (if (< (:status resp) 299)
      (do (swap! db update-in path merge (if unbundle
                                           (merge {:loading false} (do-unbundle data-key map-f doc))
                                           {:loading false data-key doc}))
          (when success
            (dispatch-event success (if unbundle
                                      (merge {:response resp} (do-unbundle data-key map-f doc))
                                      {:response resp :data doc}))))
      (do (swap! db update-in path merge {:loading false :error doc})
          (when error
            (dispatch-event error {:response resp :data doc}))))))

(defn http-fetch [opts]
  (if (vector? opts)
    (doseq [o opts] (when (some? o) (*http-fetch o)))
    (*http-fetch opts)))

(rf/reg-fx :json/fetch json-fetch)
(rf/reg-fx :http/fetch http-fetch)

(def dispatch rf/dispatch)
(def subscribe rf/subscribe)
(def create world/create)
(def create! world/create!)
(def truncate world/truncate)

(defmacro match-sub [sub patt]
  `(let [s# (rf/subscribe ~sub)
         res# @s#]
     (matcho/match res# ~patt)
     res#))

(defn ensure-server []
  (world/ensure-box))

(defn enable-logs []
  (klog.core/stdout-pretty-appender))

(defn disable-logs []
  (klog.core/clear-appenders))

(defn restart-server []
  (world/restart-box))

(defn contexts-diff [route old-contexts new-contexts params old-params]
  (let [n-idx new-contexts
        o-idx old-contexts
        to-dispose (reduce (fn [acc [k o-params]]
                             (let [n-params (get new-contexts k)]
                               (if (= n-params o-params)
                                 acc
                                 (conj acc [k :deinit o-params]))))
                           [] old-contexts)

        to-dispatch (reduce (fn [acc [k n-params]]
                              (let [o-params (get old-contexts k)]
                                (cond
                                  (or (nil? o-params) (not (= n-params o-params)))
                                  (conj acc [k :init n-params])
                                  (and o-params (= (:. n-params) route))
                                  (conj acc [k :return n-params])
                                  :else acc)))
                            to-dispose new-contexts)]
    to-dispatch))

(defonce page-state (atom {}))

(defn page [path & [params q-params]]
  (klog.core/log :ui/navigate-to {:path path :params params :q-params q-params})
  (reset! page-state {:path path :params params :q-params q-params})
  (if-let [route (route-map/match [:. path] app.routes/routes)]
    (let [params (merge (or params {}) (:params route))
          route {:match (:match route) :params params :parents (:parents route)}
          db @re-frame.db/app-db
          contexts (reduce (fn [acc {c-params :params ctx :context route :.}]
                             (if ctx
                               (assoc acc ctx (assoc c-params :. route))
                               acc)) {} (:parents route))
          current-page (:match route)
          old-page     (get-in db [:route-map/current-route :match])
          old-params (get-in db [:route-map/current-route :params])

          page-ctx-events (cond
                            (= current-page old-page)
                            (cond (= old-params params) []

                                  (= (dissoc old-params :params)
                                     (dissoc params :params))
                                  [[current-page :params params old-params]]

                                  :else
                                  [[current-page :deinit old-params] [current-page :init params]])
                            :else
                            (cond-> []
                              old-page (conj [old-page :deinit old-params])
                              true (conj [current-page :init params])))

          old-contexts (:route/context db)
          context-evs (contexts-diff (:match route) old-contexts contexts params old-params)]
      (if route
        (do
          (swap! re-frame.db/app-db assoc
                 :fragment path
                 :fragment-params params
                 :fragment-path path
                 :fragment-query-string q-params
                 :route/context contexts
                 :route-map/current-route route)
          (doseq [ev (into context-evs page-ctx-events)]
            (klog.core/log :rf/dispatch {:rf ev})
            (rf/dispatch ev)))
        (swap! re-frame.db/app-db assoc
               :fragment path
               :route-map/current-route nil
               :route-map/current-route nil
               :route-map/error :not-found)))))

(defn open [& args]
  (reset! app-db {})
  (apply page args))

(defn parse-querystring [s]
  (-> (str/replace s #"^\?" "")
      (str/split #"&")
      (->>
       (reduce (fn [acc kv]
                 (let [[k v] (str/split kv #"=" 2)]
                   (assoc acc (keyword k) v))) ;;TODO entity decoder
               {}))))

(defn gen-query-string [params]
  (->> params
       (map (fn [[k v]] (str (name k) "=" v))) ;;TODO
       (str/join "&")
       (str "?")))

(rf/reg-fx :zframes.redirect/redirect
           (fn [opts]
             (let [[uri qs] (str/split (:uri opts) #"\?" 2)
                   uri (if (str/starts-with? uri "#") (subs uri 1) uri)
                   params (when qs (parse-querystring qs))]
               (open uri {:params (merge (:params opts) params)}))))


(rf/reg-fx :zframes.redirect/merge-params
           (fn [params]
             (let [pth        (get @app-db :fragment-path)
                   nil-keys (reduce (fn [acc [k v]]
                                      (if (nil? v) (conj acc k) acc)) [] params)
                   old-params (or (get-in @app-db [:fragment-params :params]) {})]
               (page pth {:params (apply dissoc (merge old-params params)
                                         nil-keys)}))))

(rf/reg-fx
 :keybind/bind
 (fn [& args]
   (klog.core/log :keybind/bind {:args args})
   {}))

(rf/reg-cofx
 :storage/get
 (fn [& args]
   (klog.core/log :storage/get {:args args})
   {}))

(rf/reg-fx :storage/set
 (fn [& args]
   (klog.core/log :storage/set {:args args})
   {}))


(rf/reg-fx :dispatch-debounce
           (fn [args]
             (rf/dispatch (:event args))))

(rf/reg-event-fx :zframes.redirect/merge-params (fn [_ [_ opts]] {:zframes.redirect/merge-params opts}))

(defn set-params [{new-params :params}]
  (let [{path :path params :params q-params :q-params} @page-state]
    (page path (update params :params merge new-params) q-params)))

(rf/reg-fx :zfr/set-params set-params)

(defn zf-set-value [root path value]
  (dispatch [:zf.core/set-value {:zf/root root :zf/path path} value]))

(defn zf-value [root path]
  (zf/value @app-db {:zf/root root :zf/path path}))

(defmacro zf-match-value [root path patt]
  `(let [v# (zf-value ~root ~path)]
     (matcho.core/match v# ~patt)
     v#))

(defn radio-pick [root path v]
  (dispatch [:anti/radiogroup-pick {:zf/root root :zf/path path} {:value v}]))

(defn select-search [root path q]
  (dispatch [:anti/select-search {:zf/root root :zf/path path} q]))

(defn select-first [root path]
  (dispatch [:anti/select-selection {:zf/root root :zf/path path}]))

(defn select-dropdown-value [root path value]
  (dispatch [:anti/dropdown-select-pick {:zf/root root :zf/path path } value]))

(defn add-tag [root path value]
  (dispatch [:anti/tagslist-new-tag {:zf/root root :zf/path path } value]))

(defn remove-tag [root path value]
  (dispatch [:anti/tagslist-delete-tag {:zf/root root :zf/path path } value]))

(defn render [c & params]
  (if-not (fn? c)
    c
    (let [res (apply c params)
          res (if (and (not (vector? res)) (fn? res))
                (apply res params)
                res)]
      (postwalk (fn [x]
                  (if (and (vector? x)
                           (not (keyword? (first x))))
                    (apply render x)
                    x)
                  ) res))))

(defn logs-on []
  (klog.core/stdout-pretty-appender))

(defn logs-off []
  (klog.core/clear-appenders))




