(ns build
  (:require [cambada.uberjar :as uberjar]
            [shadow.cljs.devtools.api :as shadow]
            [stylo.core]
            [clojure.java.io :as io]))


(defn build-ui []
  (println "Compiling clojurescript")
  (shadow/release! :app)
  (let [f (io/file "target/stylo/release/public/css/stylo.css")]
    (io/make-parents f)
    (spit f (stylo.core/compile-styles @stylo.core/styles))))


(defn build []
  (println "Building uberjar")
  (uberjar/-main
   "-a" "all"
   "-p" "resources:target/shadow/release:target/stylo/release"
   "--out" "target/uberjar"
   "--app-group-id" "app"
   "--app-artifact-id" "app"
   "--app-version" "0.0.1"
   "-m" "clojure.main"))


(defn -main []
  (build-ui)
  (build)
  (System/exit 0))


(comment
  (build-ui)
  (build))
