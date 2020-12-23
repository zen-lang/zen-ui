(ns zen-ui.web.core
  (:require
   [clojure.string :as str]
   [org.httpkit.server :as http-kit]
   [ring.util.codec :as codec]
   [zen-ui.web.formats]
   [ring.middleware.cookies :as cookies]
   [ring.util.response]
   [ring.util.request]
   [ring.middleware.head]
   [clj-yaml.core]
   [clojure.walk])
  (:use [ring.middleware.resource]
        [ring.middleware.file]
        [ring.middleware.content-type]
        [ring.middleware.not-modified]))

(defn form-decode [s] (clojure.walk/keywordize-keys (ring.util.codec/form-decode s)))

(defn prepare-request [{meth :request-method qs :query-string body :body ct :content-type headers :headers :as req}]
  (let [params (when qs (form-decode qs))
        params (if (string? params) {(keyword params) nil} params)
        method-override (and (= :post meth) (get headers "x-http-method-override"))
        body (zen-ui.web.formats/parse-body req)]
    (cond-> req
      body (merge body)
      method-override (assoc :request-method (keyword (str/lower-case method-override)))
      params (update :params merge (or params {})))))


(defn preflight
  [{meth :request-method hs :headers :as req}]
  (let [headers (get hs "access-control-request-headers")
        origin (get hs "origin")
        meth  (get hs "access-control-request-method")]
    {:status 200
     :headers {"Access-Control-Allow-Headers" headers
               "Access-Control-Allow-Methods" meth
               "Access-Control-Allow-Origin" origin
               "Access-Control-Allow-Credentials" "true"
               "Access-Control-Expose-Headers" "Location, Transaction-Meta, Content-Location, Category, Content-Type, X-total-count"}}))

(defn allow [resp req]
  (if-let [origin (get-in req [:headers "origin"])]
    (update resp :headers merge
            {"Access-Control-Allow-Origin" origin
             "Access-Control-Allow-Credentials" "true"
             "Access-Control-Expose-Headers" "Location, Content-Location, Category, Content-Type, X-total-count"})
    resp))

(defn healthcheck [h]
  (fn [req]
    (if  (and (= :get (:request-method req))
              (= "/__healthcheck" (:uri req)))
      {:status 200 :body "healthy" :headers {"content-type" "text/htlm"}}
      (h req))))

(defn mk-handler [dispatch]
  (fn [req]
    (if (= :options (:request-method req))
      (preflight req)
      (let [req (prepare-request req)
            resp (dispatch req)]
        (-> resp
            (zen-ui.web.formats/format-response req)
            (allow req))))))

(defn handle-static [h {meth :request-method uri :uri :as req}]
  (if (and (#{:get :head} meth)
           (or (str/starts-with? (or uri "") "/static/")
               (str/starts-with? (or uri "") "/favicon.ico")))
    (let [opts {:root "public"
                :index-files? true
                :allow-symlinks? true}
          path (subs (codec/url-decode (:uri req)) 8)]
      (-> (ring.util.response/resource-response path opts)
          (ring.middleware.head/head-response req)))
    (h req)))

(defn wrap-static [h]
  (fn [req]
    (handle-static h req)))

(defn start
  "start server with dynamic metadata"
  [config dispatch]
  (let [web-config (merge {:port 8080
                           :worker-name-prefix "w"
                           :thread 8
                           :max-body 20971520} config)
        web-config (update web-config :port (fn [x] (if (string? x) (Integer/parseInt x) x)))
        handler (-> (mk-handler dispatch)
                    healthcheck
                    (cookies/wrap-cookies)
                    (wrap-static)
                    (wrap-content-type {:mime-types {nil "text/html"}})
                    wrap-not-modified)]
    (println "Starting web server: \n" (clj-yaml.core/generate-string web-config) "\n")
    (http-kit/run-server handler web-config)))


(defn stop [server]
  (server))
