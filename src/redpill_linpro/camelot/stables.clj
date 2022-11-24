(ns redpill-linpro.camelot.stables
  (:require [clj-camel.core :as c]
            [clojure.tools.logging :as log]
            [clojure.tools.namespace.repl :as ctnr])
  (:gen-class))
(ctnr/disable-unload!)
; glue code for camel
(defonce camel (atom {:context nil :producer nil :consumer nil}))
(defn camel-receive
  "this is a blocking wait receiver!"
  []
  (.receiveBody (:consumer @camel)
                "direct:clojureBridge" 5000))

(defn bind-interop []
  (let [context (:context @camel)]
    (.getEndpoint context "direct:clojureBridge")))

(defn camelize-keyword-route [keyword]
  (let [prefix (namespace keyword)
        path (name keyword)]
    (str prefix ":" path)))
(defn- ns-publics-list [ns] (#(list (ns-name %) (map first (ns-publics %))) ns))

;(defn create-routes [s]
;  (let [routes (into-array (vals (ns-publics s)))
;        ctx (:context @camel)]
;  (c/add-routes ctx routes)))
  (defn create-routes [ctx & routes]
    (log/debug (class (first routes)))
    (apply c/add-routes ctx routes))

(defn camel-route [route body & [headers]]
  (let [camel-route (camelize-keyword-route route)
        pd (:producer @camel)
        result (.requestBodyAndHeaders pd camel-route body (java.util.HashMap. {}))]
    result))
