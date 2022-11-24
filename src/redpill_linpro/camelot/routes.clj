(ns redpill-linpro.camelot.routes
  (:require [clj-camel.core :as c]
            [clj-camel.util :as u]
            [redpill-linpro.camelot.stables :as stables]
            [muuntaja.core :as m])
  (:gen-class))

(defn generate-hello []
  (->> {:msg "Welcome to the new World"}
       (m/encode "application/json")
       slurp))


; define a camel route
(def test-route (c/route-builder (c/from "direct:test")
                                 (c/log "test-route: inbound body is ${body} and headers are ${headers}")
                                 (c/log "does this work?")
                                 (c/set-body (reify org.apache.camel.Expression
                                               (evaluate [this exchange type]
                                                 (generate-hello))))))
