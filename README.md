# test.calculus

Generative integration tests using test.check.

Define integration tests that look like this:

```clojure
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
```

...by definining "fixtures" using arbirtrary test.check generators and "relationships" as simple Clojure functions.
Here are some that have been defined using prismatic schema generators:

```clojure

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
```

## Developing test.calculus

`test.calculus` uses `docker-compose` to provision a test database. Run `docker-compose up -d` (or `make up`) to start it in the background, then `lein repl`. The port of the dockerized mysql will be automatically injected into the project's `env` using `lein-docker-compose`.
