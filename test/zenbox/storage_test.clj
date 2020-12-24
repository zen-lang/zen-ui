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

  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/insert-patient :params {}})
   {:error [{:message ":resourceType is required",
              :type "require",
              :path [:resourceType],
              :schema ['fhir/patient :confirms 'fhir/reference :require]}
             {:message ":id is required",
              :type "require",
              :path [:id],
              :schema ['fhir/patient :confirms 'fhir/reference :require]}]})
  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/read-patient :params {}})
   {:error [{:message ":resourceType is required",
              :type "require",
              :path [:resourceType],
              :schema ['fhir/reference :require]}
             {:message ":id is required",
              :type "require",
              :path [:id],
              :schema ['fhir/reference :require]}]
    })
  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/delete-patient :params sample-valid-patinet})
   {:error [{:message "resource doesn't exists"}]})

  (matcho/match
   (zenbox/rpc-call ctx {:method 'demo/delete-patient :params {}})
   {:error
    [{:message ":resourceType is required",
      :type "require",
      :path [:resourceType],
      :schema ['fhir/reference :require]}
     {:message ":id is required",
      :type "require",
      :path [:id],
      :schema ['fhir/reference :require]}]})


  )
