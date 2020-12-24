(ns zenbox.storage-test
  (:require [zenbox.storage :as sut]
            [zenbox.core :as zenbox]
            [zen.core :as zen]
            [clojure.test :refer [deftest is]]
            [matcho.core :as matcho]))

(deftest test-storage
  (def ctx (zen/new-context))
  (zen/read-ns ctx 'demo)

  (def sample-valid-patinet {:resourceType "Patient" :id "patient"})

  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/insert-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/read-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/delete-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/read-patient :params sample-valid-patinet})
   {:result nil})
  )
