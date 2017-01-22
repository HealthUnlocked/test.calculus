(ns example.test.helper
  (:require [clj-time.core :as time]
            [clj-time.coerce :as coerce]
            [clojure.java.jdbc :as jdbc]
            [clojure.test.check.generators :as gen]
            [environ.core :refer [env]]
            [example.forums.schemas :refer [Forum]]
            [example.posts.schemas :refer [Post]]
            [example.users.schemas :refer [User]]
            [schema.core :as s]
            [schema-generators.generators :as sgen]
            [healthunlocked.test.calculus :as tc]))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v] (coerce/to-sql-time v)))

(def test-db
  {:host      "localhost"
   :port      (env :docker-mysql-port-3306)
   :dbtype    "mysql"
   :dbname    "example_test"
   :user      "example_test"
   :password  "password"
   :classname "com.mysql.cj.jdbc.Driver"
   :useSSL    false})

(def datetime
  (gen/fmap
    #(-> (coerce/from-long %)
         (time/floor time/milli)
         (coerce/to-date))
     gen/pos-int))

(def leaf-generators
  {s/Int  gen/s-pos-int
   s/Inst datetime})

(tc/fixture :users
  {:table      :user
   :unique-key :user_id
   :generator  (sgen/generator User leaf-generators)})

(tc/fixture :forums
  {:table      :forum
   :unique-key :forum_id
   :generator  (sgen/generator Forum leaf-generators)})

(tc/fixture :posts
  {:table      :post
   :unique-key :post_id
   :generator  (sgen/generator Post leaf-generators)})

(tc/one-to-many :author
  (fn [[user forum] post]
    (assoc post
           :user_id  (:user_id user)
           :forum_id (:forum_id forum))))

(tc/many-to-many :member
  {:table :membership}
  (fn [forum user]
    {:forum_id (:forum_id forum)
     :user_id  (:user_id user)}))
