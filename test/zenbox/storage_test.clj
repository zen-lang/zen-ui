(ns zenbox.storage-test
  (:require [zenbox.storage :as sut]
            [zenbox.core :as zenbox]
            [zen.core :as zen]
            [clojure.test :refer [deftest is]]
            [matcho.core :as matcho]))

(deftest test-storage
  (def ctx (zen/new-context))
  (zen/read-ns ctx 'demo)
  (zenbox/rpc-call ctx {:method 'demo/insert-patient})

  )
