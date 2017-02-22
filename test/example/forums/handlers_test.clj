(ns example.forums.handlers-test
  (:require [clojure.test :as t :refer [deftest is are run-tests]]
            [clojure.test.check.generators :as gen]
            [environ.core :refer [env]]
            [example.forums.handlers :as h]
            [example.test.helper :as th]
            [healthunlocked.test.calculus :as tc]))

(tc/use-test-db th/test-db)

(deftest forum-posts-test
  ; (macroexpand-1 '
                 (tc/integration-test "get-posts" 10

    ;; 'fixtures' are specified first. these are records that will be
    ;; generated from user defined generators and inserted into the DB before
    ;; the tests are run.
    [:users  [dinesh               {:username "dinesh"}
              aamira               {:username "aamira7"}
              charlotte            {:username "CH4Z"}
              shonda               {:date_created (java.util.Date.)}
              hideo                {:email (str (:username charlotte) "@foo.com")}]

     :forums [funny-cat-gifs       {}
              politics             {}
              clojure-enthusiasts  {}]

     :posts  [my-cat               {:user_id (:user_id charlotte) :forum_id (:forum_id funny-cat-gifs)}
              resisting-trump      {:user_id (:user_id hideo)     :forum_id (:forum_id politics)}
              macro-confusion      {:user_id (:user_id dinesh)    :forum_id (:forum_id clojure-enthusiasts)}
              cat-cafes            {:user_id (:user_id shonda)    :forum_id (:forum_id funny-cat-gifs)}
              parenthesis-overload {:user_id (:user_id aamira)    :forum_id (:forum_id clojure-enthusiasts)}]]

    ;; 'relationships' follow the fixtures. these trigger the use of
    ;; user-defined functions that set foreign keys/create the join table
    ;; records required to specify the given relationship in the db.
    [:member funny-cat-gifs      dinesh charlotte shonda]
    [:member politics            aamira shonda hideo]
    [:member clojure-enthusiasts dinesh aamira]

    ;; 'generators' - any other arbitrary generators that are needed can be
    ;; specified here. these are not really needed for the below test, but are
    ;; just there as an example.
    [x gen/s-pos-int
     y gen/s-pos-int]

    (is (> (+ x y) x))
    (is (> (+ x y) y))

    (is (= (sort-by :post_id [macro-confusion parenthesis-overload])
           (sort-by :post_id (h/get-posts (tc/test-db) clojure-enthusiasts))))))
; )
