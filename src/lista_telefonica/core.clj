(ns lista-telefonica.core
  (:require [clojure.data.json :as json]
            [clojure.string :refer [blank?]])

  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;;;;;;;;; CONSTANTS ;;;;;;;;;;;

(def data-path "resources/data.json")


;;;;;;;;; HELPER FUNCTIONS ;;;;;;;;;


(defn reader-data []
  (let [data-string (slurp data-path)]
    (with-open [reader (clojure.java.io/reader data-path)]
      (if (blank? data-string)
        []
        (json/read reader :key-fn keyword)))))




(defn writer-data [data]
  (with-open [writer (clojure.java.io/writer data-path)]
    (json/write data writer)))


;;;;;;;;;;; CREATE ;;;;;;;;;;;;;


(defn create-entry [entry]
  (let [original-data (reader-data)
        new-data  (conj original-data entry)]
    (do (writer-data new-data)
      (reader-data))))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-entry [email]
  (let [original-data (reader-data)]
    (first (filter #(= email (:email %)) original-data))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-entry [{:keys [email to-update new-value]}]
  (let [original-data (reader-data)
        new-data  (mapv #(if (= (:email %) email)
                           (assoc % to-update new-value)
                           %) original-data)]
    (do (writer-data new-data) (get-entry email))))





(defn delete-entry [email]
  (let [original-data (reader-data)
        new-data  (remove #(= (:email %) email) original-data)]
    (do (writer-data new-data) (reader-data))))









#_(with-open [reader (clojure.java.io/reader "resources/color.json")]
    (json/read reader :key-fn keyword))

#_(with-open [writer (clojure.java.io/writer "resources/data.json")]
    (json/write [{:teste "calor"}] writer))
