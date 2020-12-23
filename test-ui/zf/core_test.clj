(ns zf.core-test
  (:require
   [zf.core :as zf]
   [matcho.core :as matcho]
   [clojure.test :refer :all]))

(def schema1
  {:zf/keys {:name {}
             :address {:zf/keys {:city {}}}
             :roles   {:zf/items {}}
             :refs    {:zf/items {:zf/keys {:display {}
                                            :id {}}}}}})



(deftest test-zf
  (is (= "_a_b_c" (zf/get-id {:zf/root [:a :b] :zf/path [:c]})))
  (is (= "_fqn_keyword_b_c" (zf/get-id {:zf/root [:fqn/keyword :b] :zf/path [:c]}))))
