(ns erl-like-app.todo.todo-server
  (:require

    [clojure.core.match :refer [match]]

    [taoensso.timbre :as log]

    [otplike.process :as process]
    [otplike.gen-server :as gen-server]

    [erl-like-app.todo.entry :as entry]
    [erl-like-app.todo.db :as db]))

;;;;;;;;;;;;; private

(def ^:private SERVER_NAME
  :todo)

(defn- call*
  ;;
  ([message timeout]
   (gen-server/call (process/whereis SERVER_NAME) message timeout))
  ;;
  ([message]
   (call* message 5000)))

(defn- cast* [message]
  (gen-server/cast (process/whereis SERVER_NAME) message))

(defn get-state []
  (gen-server/get SERVER_NAME))


;;;;;;;;;;;;; otp

(defn start-link [params]
  (gen-server/start-ns [params] {:spawn-opt {:link true
                                             :register  SERVER_NAME
                                             :flags     {:trap-exit true}}}))

(defn init [_params]
  (let [state {:counter 0,
               :db {}}]
    (log/info (name SERVER_NAME) "server initialized")
    [:ok state]))

;;;;;;;;;;;;; API

;; -> [:ok td] | [:error reason]
(defn create-todo [params]
  (call* [:create-todo params]))

;; -> [:ok td] | [:error reason]
(defn find-todo-by-id [id]
  (call* [:find-todo-by-id id]))

;; -> [:ok updated_td] | [:error reason]
(defn terminate-todo [id]
  (call* [:terminate-todo id]))

;; -> [:ok nil] | [:error reason]
(defn delete-todo [id]
  (call* [:delete-todo id]))

;; -> [:ok tds] | [:error reason]
(defn enumerate-active-todos []
  (call* :enumerate-active-todos))

;;;;;;;;;;;;; core

;; -> [id updated_state]
(defn- next-id [{:keys [counter] :as state}]
  [(-> counter inc str)
   (update state :counter inc)])


(defn- create-todo* [params state]
  (let [[id updated_state] (next-id state)
        todo_entry (entry/create id params)
        updated_state (-> (db/update-todo todo_entry updated_state) second)
        reply (db/find-todo-by-id id updated_state)]
    [:reply reply updated_state]))

(defn- find-todo-by-id* [id state]
  (let [reply (db/find-todo-by-id id state)]
    [:reply reply state]))

(defn- terminate-todo* [id state]
  (let [[reply updated_state]  (match (db/find-todo-by-id id state)

                                      [:ok loaded]
                                      (let [updated_todo (entry/terminate loaded)
                                            updated_state (-> (db/update-todo updated_todo state) second)]
                                        [(db/find-todo-by-id id updated_state)
                                         updated_state])

                                      ([:error reason] :as r)
                                      [r state])]
    [:reply reply updated_state]))


(defn- delete-todo* [id state]
  (let [updated_state (-> (db/delete-todo id state) second)
        reply [:ok nil]]
    [:reply reply updated_state]))


(defn- enumerate-active-todos* [state]
  (let [reply (db/enumerate-active-todos state)]
    [:reply reply state]))

;;;;;;;;;;;;; otp

(defn handle-call [message _from state]
  (match message

         [:create-todo params]
         (create-todo* params state)

         [:find-todo-by-id id]
         (find-todo-by-id* id state)

         [:terminate-todo id]
         (terminate-todo* id state)

         [:delete-todo id]
         (delete-todo* id state)

         :enumerate-active-todos
         (enumerate-active-todos* state)))


(defn handle-cast [message state]
  (log/error "unknown cast:" message)
  [:noreply state])


(defn handle-info [message state]
  (log/error "unknown info:" message)
  [:noreply state])


(defn terminate [_reason _state]
  (log/info (name SERVER_NAME) "server stopped"))
