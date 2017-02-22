(ns healthunlocked.test.calculus
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.string :as str]
            [clojure.test :as t]
            [clojure.test.check.generators :as gen]
            [com.gfredericks.test.chuck.clojure-test :as chuck]
            [com.stuartsierra.component :as component]))

(def ^:dynamic *system* nil)

(defmacro use-test-system
  [system]
  `(t/use-fixtures
     :once
     (fn [f#]
       (binding [*system* (component/start (~system))]
         (try (f#)
           (finally
             (component/stop *system*)))))))

(defmacro use-test-db
  [db]
  `(let [db# ~db]
     (use-test-system (constantly {:db db#}))))

(defn test-system [] *system*)
(defn test-db     [] (:db *system*))

(defmacro with-test-transaction
  [[db-binding] & forms]
  `(jdbc/with-db-transaction [tx# (:db *system*)]
     (jdbc/db-set-rollback-only! tx#)
     (binding [*system* (assoc *system* :db tx#)]
       (let [~db-binding tx#]
         ~@forms))))

(defmulti fixture-conf identity)
(defmulti relate-fixtures (fn [[k & records] extra-records] k))

(defn fixture-gen
  [k]
  (let [{:keys [generator table]} (fixture-conf k)]
    (gen/fmap
      #(vary-meta % assoc ::table table)
      (gen/no-shrink generator))))

(defn enforce-uniqueness
  [k & ms]
  (let [{:keys [unique-key]} (fixture-conf k)]
    (gen/fmap (partial map (fn [m id] (assoc m unique-key id)) ms)
              (gen/vector-distinct gen/s-pos-int {:num-elements (count ms)}))))

(defn- sql-quote
  [k]
  (if (and (string? k) (str/starts-with? k "`"))
    k
    (str "`" (name k) "`")))

(defn save-fixtures!
  [db & records]
  (doseq [records-of-type (partition-by (comp ::table meta) records)]
    (jdbc/insert-multi! db
                        (-> records-of-type first meta ::table sql-quote)
                        records-of-type)))

(defmacro integration-test
  "I'm going to go to hell for this."
  [description attempts fixtures & relationships+generators+tests]

  (let [[relationships+generators tests]
        (split-with vector? relationships+generators+tests)

        relationships
        (if (keyword? (first (last relationships+generators)))
          relationships+generators
          (butlast relationships+generators))

        generators
        (if (keyword? (first (last relationships+generators)))
          []
          (last relationships+generators))

        extra-records-binding
        (gensym "extra-records-")

        records-to-save
        (take-nth 2 (mapcat second (partition 2 fixtures)))]

    `(chuck/checking ~description (chuck/times ~attempts)
       [

        ;; first, let the specified names using basic, unadorned generators
        ~@(apply concat (for [[k bindings] (partition 2 fixtures)
                              [b m]        (partition 2 bindings)]
                          [b `(fixture-gen ~k)]))

        ;; then, assoc in any fields that should be unique to avoid clashes
        ~@(mapcat (juxt (comp vec (partial take-nth 2) second)
                        (fn [[k bindings]]
                          `(enforce-uniqueness ~k ~@(take-nth 2 bindings))))
                  (partition 2 fixtures))

        ;; finally, assoc in the specified default values
        ~@(mapcat (juxt (comp vec (partial take-nth 2) second)
                        (fn [[k bindings]]
                          `(gen/return (map merge [~@(take-nth 2 bindings)]
                                                  [~@(take-nth 2 (rest bindings))]))))
                  (partition 2 fixtures))

        ~@generators]

       (let [~extra-records-binding []
             ~@(mapcat (juxt (juxt (comp vec rest) (constantly extra-records-binding))
                             (fn [rel] `(relate-fixtures ~rel ~extra-records-binding))) relationships)]
         (with-test-transaction [db#]
           (apply save-fixtures! db# ~@records-to-save ~extra-records-binding)
           ~@tests)))))

(defn fixture
  [k config]
  (defmethod fixture-conf k
    [_] config))

(defn one-to-many
  [k f]
  (defmethod relate-fixtures k
    [[_ a & bs] extra-records]
    [(cons a (map (partial f a) bs))
     extra-records]))

(defn many-to-many
  [k {:keys [table] :as config} f]
  (defmethod relate-fixtures k
    [[_ a & bs] extra-records]
    [(cons a bs)
     (concat extra-records
             (map #(vary-meta (f a %) assoc ::table table) bs))]))
