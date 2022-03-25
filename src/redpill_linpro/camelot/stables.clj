(ns redpill-linpro.camelot.stables
  (:require [clj-camel.core :as c]
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

(defn camel-route [route body & [headers]]
  (let [pd (:producer @camel)]
    (c/send-body-and-headers pd route body (or headers {}))))

