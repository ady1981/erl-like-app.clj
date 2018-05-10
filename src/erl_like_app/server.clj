(ns erl-like-app.server
  (:require

    [clojure.core.match :refer [match]]

    [taoensso.timbre :as log]

    [otplike.process :as process]
    [otplike.supervisor :as supervisor]

    [erl-like-app.config :as config]
    [erl-like-app.todo-server :as todo-server])

  (:gen-class))

;;;;;;;;;;;;;;;;;;;;;;;;; supervision-tree

(defn- app-sup [_config]
  [:ok
   [{:strategy :one-for-one}
    [{:id :todo-server :start [todo-server/start-link [{}]]}]]])


(defn- start-app-sup-link [config]
  (supervisor/start-link :app-sup
                         app-sup
                         [config]))


(defn- start-boot-sup-link [config]
  (supervisor/start-link :boot-sup
                         (fn [cfg]
                           [:ok
                            [{:strategy :one-for-all}
                             [{:id :app-sup :start [start-app-sup-link [cfg]]}]]])
                         [config]))


(defn start []
  (if-let [pid (process/whereis :boot-proc)]

    (log/info "already started" pid)

    (let [config (config/get-config)]
      (process/spawn-opt
        (process/proc-fn []

                         (match (start-boot-sup-link config)

                                [:ok pid]
                                (loop []
                                  (process/receive!

                                    :restart
                                    (do
                                      (log/info "------------------- RESTARTING -------------------")
                                      (supervisor/terminate-child pid :app-sup)
                                      (log/info "--------------------------------------------------")
                                      (supervisor/restart-child pid :app-sup)
                                      (recur))

                                    :stop
                                    (process/exit :normal)))

                                [:error reason]
                                (log/error "cannot start root supervisor: " {:reason reason})))
        {:register :boot-proc}))))

(defn stop []
  (if-let [pid (process/whereis :boot-proc)]
    (process/! pid :stop)
    (log/info "already stopped")))

(defn restart []
  (if-let [pid (process/whereis :boot-proc)]
    (process/! pid :restart)
    (start)))

;;;;;;;;;;;;;;;;;;;;;;; main

(defn -main [& args]

  (start)

  ;; wait till booter-proc terminate
  (loop []
    (when (process/whereis :boot-proc)
      (Thread/sleep 1000)
      (recur))))