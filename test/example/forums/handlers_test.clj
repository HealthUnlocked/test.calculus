(ns example.forums.handlers-test
  (:require [clojure.test :as t :refer [deftest is are run-tests]]
            [clojure.test.check.generators :as gen]
            [environ.core :refer [env]]
            [example.forums.handlers :as h]
            [example.test.helper :as th]
            [test.calculus :as tc]))

(tc/use-test-db th/test-db)

(deftest forum-posts-test
  (tc/integration-test "get-posts" 10

    ;; 'fixtures' are specified first. these are records that will be
    ;; generated from user defined generators and inserted into the DB before
    ;; the tests are run.
    [:users  [dinesh aamira charlotte shonda hideo]
     :forums [funny-cat-gifs politics clojure-enthusiasts]
     :posts  [my-cat resisting-trump macro-confusion cat-cafes parenthesis-overload]]

    ;; 'relationships' follow the fixtures. these trigger the use of
    ;; user-defined functions that set foreign keys/create the join table
    ;; records required to specify the given relationship in the db.
    [:member funny-cat-gifs      dinesh charlotte shonda]
    [:member politics            aamira shonda hideo]
    [:member clojure-enthusiasts dinesh aamira]

    [:author [charlotte funny-cat-gifs]      my-cat]
    [:author [shonda    funny-cat-gifs]      cat-cafes]
    [:author [hideo     politics]            resisting-trump]
    [:author [dinesh    clojure-enthusiasts] macro-confusion]
    [:author [aamira    clojure-enthusiasts] parenthesis-overload]

    ;; 'generators' - any other arbitrary generators that are needed can be
    ;; specified here. these are not really needed for the below test, but are
    ;; just there as an example.
    [x gen/s-pos-int
     y gen/s-pos-int]

    (is (> (+ x y) x))
    (is (> (+ x y) y))

    (is (= (sort-by :post_id [macro-confusion parenthesis-overload])
           (sort-by :post_id (h/get-posts (tc/test-db) clojure-enthusiasts))))))
