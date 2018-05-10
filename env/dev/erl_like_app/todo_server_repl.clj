(ns erl-like-app.todo-server-repl
  (:require
    [erl-like-app.server :as server]
    [erl-like-app.todo.todo-server :refer :all]))

#_(server/restart)

#_(create-todo {:title "Task #1"})

#_(find-todo-by-id "1")

#_(terminate-todo "2")

#_(delete-todo nil)

#_(enumerate-active-todos)

;;;;

#_(get-state)
