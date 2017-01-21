(ns example.posts.schemas
  (:require [schema.core :as s]))

(s/defschema Post
  {:post_id      s/Int
   :forum_id     s/Int
   :user_id      s/Int
   :title        s/Str
   :body         s/Str
   :date_created s/Inst})
