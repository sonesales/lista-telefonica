(ns lista-telefonica.core-test
  (:require [clojure.test :refer :all]
            [lista-telefonica.core :refer :all]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :refer [blank?]]))

;;; CONSTANTS ;;;

(def test-data-path "resources/test.json")

;;; HELPER FUNCTIONS ;;;

(defn get-data []
  (let [data-string (slurp test-data-path)]
    (with-open [reader (clojure.java.io/reader test-data-path)]
      (if (blank? data-string)
        []
        (json/read reader :key-fn keyword)))))

;;; FIXTURES ;;;
(defn json-fixture [f]
  (spit test-data-path "")
  (with-redefs [data-path test-data-path]
    (f))
  (io/delete-file test-data-path))

(use-fixtures :once json-fixture)

;;; TESTS ;;;

(deftest create-entry-test
  (testing "Testando a funcao create-entry"
    (let [test-entry {:name "João da Silva", :number "4499999999", :email "joao@gmail.com"}
          result    (create-entry test-entry)
          json-data (get-data)]
      (is (= [test-entry] result))
      (is (= (count json-data) 1))
      (is (= (first json-data) test-entry)))))


(deftest json-data
  (testing "Usando a create-entry para testar se o json esta gravando multiplos dados"
    (let [test-entry-1 {:name "Lucas", :number "1199999999", :email "lucas@gmail.com"}
          test-entry-2 {:name "Paulo", :number "1199999999", :email "paulo@gmail.com"}
          _         (do (create-entry test-entry-1) (create-entry test-entry-2))
          json-data (get-data)]
      (is (= (count json-data) 2))
      (is (= (first json-data) test-entry-1)) ;; POSSIBLE FIXME: check later if data is written and read in order:w
      (is (= (second json-data) test-entry-2)))))


(deftest delete-entry-test
  (testing "Testando a função delete-entry"
    (let [test-entry-1 {:name "Lucas", :number "1199999999", :email "lucas@gmail.com"}
          test-entry-2 {:name "Paulo", :number "1199999999", :email "paulo@gmail.com"}
          _ (do (create-entry test-entry-1) (create-entry test-entry-2))
          result (delete-entry "paulo@gmail.com")
          json-data (get-data)]
      (is (= (:name (first result)) "Lucas"))
      (is (= (first json-data) test-entry-1))
      (is (= (count json-data) 1)))))

(deftest get-entry-test
  (testing "Testando a função get-entry"
    (let [test-entry-1 {:name "Lucas", :number "1199999999", :email "lucas@gmail.com"}
          test-entry-2 {:name "Paulo", :number "1199999999", :email "paulo@gmail.com"}
          _ (do (create-entry test-entry-1) (create-entry test-entry-2))
          result (get-entry "paulo@gmail.com")]
      (is (= (:name result) "Paulo")))))

(deftest update-entry-test
  (testing "Testando a função update-entry"
    (let [test-entry-1 {:name "Lucas", :number "1199999999", :email "lucas@gmail.com"}
          test-entry-2 {:name "Paulo", :number "1199999999", :email "paulo@gmail.com"}
          _ (do (create-entry test-entry-1) (create-entry test-entry-2))
          result (update-entry  {:email "lucas@gmail.com", :to-update :name, :new-value "Roberto"})
          json-data (get-data)]
      (is (= (:name result) "Roberto"))
      (is (some #(= "Roberto" (:name %)) json-data)))))


(deftest integration-test
  (testing "Quando eu crio duas entries"
    (let [test-entry-1 {:name "Lucas", :number "1199999999", :email "lucas@gmail.com"}
          test-entry-2 {:name "Paulo", :number "1199999999", :email "paulo@gmail.com"}
          _ (do (create-entry test-entry-1) (create-entry test-entry-2))
          json-data (get-data)]
      (is (and (= (count json-data) 2)
               (= (first json-data) test-entry-1)
               (= (second json-data) test-entry-2))
          "Elas devem existir tambem no json")
      (is (= (get-entry "lucas@gmail.com") test-entry-1)
          "Eu posso fazer um get em uma delas")
      (is (= (assoc test-entry-1 :name "Roberto")
             (update-entry {:email "lucas@gmail.com", :to-update :name, :new-value "Roberto"}))
          "Eu posso fazer update em uma delas")
      (is (some #(= "Roberto" (:name %)) (get-data))
          "O update deve afetar o json tambem")
      (is (not-any? #(= "Paulo" (:name %)) (delete-entry "paulo@gmail.com"))
          "Eu posso deletar uma delas")
      (is (not-any? #(= "Paulo" (:name %)) (get-data))
          "O delete deve afetar o json tambem"))))
