(ns redpill-linpro.camelot
  (:require [clj-camel.core :as c]
            [org.httpkit.server :as http]
            [reitit.ring :as ring]
            [redpill-linpro.camelot.stables :as stables]
            [ring.middleware.reload]
            [ring.middleware.defaults
             :refer [wrap-defaults api-defaults]]
            [ring.util.http-response :as response])
  (:gen-class))

; reitit + ring HTTP handling
(defn handle-get [req]
  (let [result (stables/camel-receive)]
    (stables/camel-route "direct:test" "hello from GET" {:x-header-msg "hello"})
    (response/ok (str "cool: " result))))

(def router
  (ring/router
   ["/hello" {:get handle-get}]))
(def app (ring/ring-handler router))
(def reloadable-app (ring.middleware.reload/wrap-reload #'app))

; define a camel route
(def test-route (c/route-builder (c/from "direct:test")
                                 (c/log "test-route: inbound body is ${body} and headers are ${headers}")
                                 (c/log "does this work?")
                                 (c/set-body (c/constant "Works!"))
                                 (c/to "direct:clojureBridge")
                                 (c/end)))
; let's go
(defn -main
  "I don't do a whole lot ... yet."
  [& _]
  (http/run-server
   (wrap-defaults #'reloadable-app api-defaults) {:port 8080 :join? false})
  (let [ctx (c/camel-context)
        pd (.createProducerTemplate ctx)
        cs (.createConsumerTemplate ctx)]
    (c/add-routes ctx test-route)
    (.start ctx)
    (.start cs)
                                        ; assoc the camel context to the global ctx atom
    (reset! stables/camel {:context ctx :producer pd :consumer cs})))
