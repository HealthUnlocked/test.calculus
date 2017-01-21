(ns example.forums.handlers
  (:require [clojure.java.jdbc :as jdbc]))

(defn get-posts
  [db {:keys [forum_id] :as forum}]
  (jdbc/query db ["select p.* from post p where p.forum_id = ?" forum_id]))
