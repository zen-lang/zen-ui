(ns zenbox.storage-test
  (:require [zenbox.core :as zenbox]
            [zenbox.storage.core]
            [zen.core :as zen]
            [zen.store :as zen-extra]
            [clojure.test :refer [deftest is]]
            [matcho.core :as matcho]))


(deftest test-storage
  (def ctx (zen/new-context))
  (zen/read-ns ctx 'demo)

  (def sample-valid-patinet {:resourceType "Patient" :id "patient"})


  (matcho/match
   (zenbox/rpc ctx {:method 'demo/insert-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (zenbox/rpc ctx {:method 'demo/read-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (zenbox/rpc ctx {:method 'demo/delete-patient :params sample-valid-patinet})
   {:result sample-valid-patinet})

  (matcho/match
   (zenbox/rpc ctx {:method 'demo/read-patient :params sample-valid-patinet})
   {:result nil})

  (matcho/match
   (zenbox/rpc ctx {:method 'demo/insert-patient :params {}})
   {:error [{:message ":resourceType is required",
             :type "require",
             :path [:resourceType],
             :schema ['fhir/patient :confirms 'fhir/resource :require]}
            {:message ":id is required",
             :type "require",
             :path [:id],
             :schema ['fhir/patient :confirms 'fhir/resource :require]}]})
  #_(matcho/match
   (zenbox/rpc ctx {:method 'demo/read-patient :params {}})
   {:error [{:message ":resourceType is required",
             :type "require",
             :path [:resourceType],
             :schema ['fhir/resource :require]}
            {:message ":id is required",
             :type "require",
             :path [:id],
             :schema ['fhir/resource :require]}]
    })
  #_(matcho/match
   (zenbox/rpc ctx {:method 'demo/delete-patient :params sample-valid-patinet})
   {:error [{:message "resource doesn't exists"}]})

  #_(matcho/match
   (zenbox/rpc ctx {:method 'demo/delete-patient :params {}})
   {:error
    [{:message ":resourceType is required",
      :type "require",
      :path [:resourceType],
      :schema ['fhir/resource :require]}
     {:message ":id is required",
      :type "require",
      :path [:id],
      :schema ['fhir/resource :require]}]})



  (matcho/match
   (zenbox/rpc ctx {:method 'demo/create-pgstore  :params {:zen/name 'click-house
                                                                  :user "superadmin"
                                                                  :password "123"
                                                                  :host "clickhouse-db"
                                                                  :database "zenbox"
                                                                  :port 5432}})

   {:result
    {:user "superadmin",
     :password "123",
     :host "clickhouse-db",
     :port 5432,
     ;; :zen/tags #{'storage/storage 'storage/pgstore},
     :zen/name 'storage/click-house}
    })

  (matcho/match (zen/get-symbol ctx 'storage/click-house)
                {:user "superadmin",
                 :password "123",
                 :host "clickhouse-db",
                 :port 5432,
                 :database "zenbox"
                 ;; :zen/tags #{'storage/storage 'storage/pgstore},
                 :zen/name 'storage/click-house})

  (matcho/match
   (zenbox/rpc ctx {:method 'demo/create-pgstore  :params {:zen/name 'click-house}})

   {:error
    [{:message ":password is required",
      :type "require",
      :path [:password],
      :schema ['storage/pgstore :require]}
     {:message ":port is required",
      :type "require",
      :path [:port],
      :schema ['storage/pgstore :require]}
     {:message ":host is required",
      :type "require",
      :path [:host],
      :schema ['storage/pgstore :require]}
     {:message ":database is required",
      :type "require",
      :path [:database],
      :schema ['storage/pgstore :require]}
     {:message ":user is required",
      :type "require",
      :path [:user],
      :schema ['storage/pgstore :require]}]})
  )
