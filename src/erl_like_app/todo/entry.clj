(ns erl-like-app.todo.entry)

(defn- get-current-time []
  (System/currentTimeMillis))


(defn create [id params]
  (let [t (get-current-time)]
    (merge (select-keys params [:title])
           {:id id
            :created t
            :updated t
            :status :active})))

(defn terminate [entry]
  (assoc entry
         :status :terminated
         :updated (get-current-time)))