(ns user
  (:require
   [zenbox.core :as zenbox]
   [zen.core :as zen]
   [clojure.java.io :as io]
   [clojure.tools.namespace.repl :as repl]
   [shadow.cljs.devtools.api :as shadow]
   [shadow.cljs.devtools.config :as shadow.config]
   [shadow.cljs.devtools.server :as shadow.server]))

(defn restart [])

(defn delete-recursively [f]
  (when (.isDirectory f)
    (doseq [c (.listFiles f)]
      (delete-recursively c)))
  (io/delete-file f))

(defn restart-shadow-clean []
  (shadow.server/stop!)
  (try (-> (shadow.config/get-build :app)
           (get-in [:dev :output-dir])
           (io/file)
           (delete-recursively))
       (delete-recursively (io/file ".shadow-cljs"))
       (catch Exception _))
  (shadow.server/start!)
  (shadow/watch :app))

(defn restart-ui []
  (restart-shadow-clean))

(defn reload-ns []
  (repl/set-refresh-dirs "src" "src-c" "src-ui")
  (repl/refresh))

(comment



  (def ctx (zen/new-context))

  (zen/read-ns ctx 'demo)

  (zenbox/start ctx)

  (zenbox/stop ctx)

  (restart-ui)

  )

