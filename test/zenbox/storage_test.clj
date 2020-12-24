(ns zenbox.storage-test
  (:require [zenbox.storage :as sut]
            [zenbox.core :as zenbox]
            [zen.core :as zen]
            [clojure.test :refer [deftest is]]
            [matcho.core :as matcho]))


(defn operation-wrapper [ctx json-rpc]
  (:body (zenbox/operation ctx {:operation 'zenbox/json-rpc} {:resource json-rpc})))

(deftest test-storage
  (def ctx (zen/new-context))
  (zen/read-ns ctx 'demo)

  (def sample-valid-patinet {:resourceType "Patient" :id "patient"})

  (matcho/match
   (operation-wrapper ctx {:method 'demo/insert-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (operation-wrapper ctx {:method 'demo/read-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (operation-wrapper ctx {:method 'demo/delete-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (operation-wrapper ctx {:method 'demo/read-patient :params sample-valid-patinet})
   {:result nil})

  (matcho/match
   (operation-wrapper ctx {:method 'demo/insert-patient :params {}})
   {:error [{:message ":resourceType is required",
              :type "require",
              :path [:resourceType],
              :schema ['fhir/patient :confirms 'fhir/resource :require]}
             {:message ":id is required",
              :type "require",
              :path [:id],
              :schema ['fhir/patient :confirms 'fhir/resource :require]}]})
  (matcho/match
   (operation-wrapper ctx {:method 'demo/read-patient :params {}})
   {:error [{:message ":resourceType is required",
              :type "require",
              :path [:resourceType],
              :schema ['fhir/resource :require]}
             {:message ":id is required",
              :type "require",
              :path [:id],
              :schema ['fhir/resource :require]}]
    })
  (matcho/match
   (operation-wrapper ctx {:method 'demo/delete-patient :params sample-valid-patinet})
   {:error [{:message "resource doesn't exists"}]})

  (matcho/match
   (operation-wrapper ctx {:method 'demo/delete-patient :params {}})
   {:error
    [{:message ":resourceType is required",
      :type "require",
      :path [:resourceType],
      :schema ['fhir/resource :require]}
     {:message ":id is required",
      :type "require",
      :path [:id],
      :schema ['fhir/resource :require]}]})


  )
