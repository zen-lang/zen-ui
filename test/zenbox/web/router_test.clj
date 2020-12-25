(ns zenbox.web.router-test
  (:require [zenbox.web.router :as sut]
            [zen.core :as zen]
            [clojure.test :refer [deftest is]]
            [matcho.core :as matcho]))



(deftest test-router
  (def ctx (zen/new-context))

  (zen/read-ns ctx 'demo)

  (def srv (zen/get-symbol ctx 'demo/server))

  (matcho/match
   (sut/match ctx :post "/json-rpc" (select-keys srv [:apis]))
    {:match {:operation 'demo/json-rpc-op}})

  (is (nil? (sut/match ctx :post "/unexisting" (select-keys srv [:apis]))))

  (matcho/match
   (sut/match ctx :get "/zen/symbols" (select-keys srv [:apis]))
    {:match {:operation 'demo/get-symbols}})

  (matcho/match
   (sut/match ctx :get "/zen/symbols/myns.myns/mysubmol" (select-keys srv [:apis]))
    {:match {:operation 'demo/get-symbol}
     :params {:ns "myns.myns"
              :name "mysubmol"}})

  (is (=
       (sut/get-all-paths ctx)

       [{:method :GET, :operation 'demo/index-op, :uri "/"}
        {:method :POST, :operation 'demo/json-rpc-op, :uri "/json-rpc"}
        {:method :GET, :operation 'demo/get-symbols, :uri "/zen/symbols"}
        {:method :GET, :operation 'demo/get-symbol, :uri "/zen/symbols/:ns/:name"}
        {:method :GET, :operation 'demo/get-tags, :uri "/zen/tags"}]
       ))

  )
