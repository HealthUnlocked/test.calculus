(ns example.users.schemas
  (:require [schema.core :as s]))

(s/defschema User
  {:user_id      s/Int
   :username     s/Str
   :email        s/Str
   :date_created s/Inst})
