(ns erl-like-app.todo-server-repl
  (:require
    [erl-like-app.server :as server]
    [erl-like-app.todo.todo-server :refer :all]))

#_(server/restart)

#_(create-todo {:title "task #1", :description "create task #2"})
#_(create-todo {:title "task #2"})

#_(find-todo-by-id "1")

#_(terminate-todo "1")

#_(delete-todo "1")

#_(enumerate-active-todos)

;;;;

#_(get-state)
