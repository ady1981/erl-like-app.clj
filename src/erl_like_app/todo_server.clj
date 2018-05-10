(ns erl-like-app.todo-server
  (:require

    [clojure.core.match :refer [match]]

    [taoensso.timbre :as log]

    [otplike.process :as process]
    [otplike.gen-server :as gen-server]))

;;;;;;;;;;;;; otp

(def SERVER_NAME
  :todo)

(defn start-link [params]
  (gen-server/start-ns [params] {:spawn-opt {:link true
                                 :register  SERVER_NAME
                                 :name      :todo-server
                                 :flags     {:trap-exit true}}}))

(defn init [params]
  (gen-server/cast (process/self) [:init params])
  (log/info (name SERVER_NAME) "server initialized")
  [:ok {}])


;;;;;;;;;;;;; private

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

;;;;;;;;;;;;; API

;; -> [:ok example_entry] | [:error reason]
(defn create [params]
  (call* [:create params]))

;; -> [:ok example_entry] | [:error reason]
(defn find-by-id [id]
  (call* [:find-by-id id]))


;;;;;;;;;;;;; core


(defn- init* [_params]
  [:noreply {}])


(defn create-entry [params]
  (let [t (System/currentTimeMillis)]
    (merge params
           {:created t
            :updated t})))


(defn- create* [params {:keys [db] :as state}]
  (let [example_entry (create-entry {})
        reply [:error :not_implemented_yet]]
    [:reply reply state]))


(defn- find-by-id* [id {:keys [db] :as state}]
  (let [reply [:error :not_implemented_yet]]
    [:reply reply state]))


;;;;;;;;;;;;; otp

(defn handle-cast [message _state]
  (match message

         [:init params]
         (init* params)))


(defn handle-call [message _from state]
  (match message

         [:create params]
         (create* params state)

         [:find-by-id id]
         (find-by-id* id state)))


(defn terminate [_ _state]
  (log/info (name SERVER_NAME) "server stopped"))
