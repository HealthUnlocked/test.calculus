(ns example.forums.schemas
  (:require [schema.core :as s]))

(s/defschema Forum
  {:forum_id     s/Int
   :name         s/Str
   :description  s/Str
   :date_created s/Inst})

(s/defschema Membership
  {:forum_id     s/Int
   :user_id      s/Int
   :date_created s/Inst})
