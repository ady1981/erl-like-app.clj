(ns erl-like-app.todo.db
  (:require
    [erl-like-app.util :as util]))

;; -> [:ok updated_ctx]
(defn update-todo [{:keys [id] :as todo_entry} ctx]
  {:pre [id]}
  [:ok (assoc-in ctx [:db id] todo_entry)])

;; -> [:ok td] | [:error :not_found]
(defn find-todo-by-id [id ctx]
  (if-let [todo (get-in ctx [:db id])]
    [:ok todo]
    [:error :not_found]))

;; -> [:ok updated_ctx]
(defn delete-todo [id ctx]
  [:ok (util/dissoc-in ctx [:db id])])

;; -> [:ok tds]
(defn enumerate-active-todos [ctx]
  [:ok (->> ctx
            :db
            vals
            (filter #(-> % :status #{:active}))
            (sort-by :created))])