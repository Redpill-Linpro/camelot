(ns redpill-linpro.camelot
  (:require [clj-camel.core :as c]
            [org.httpkit.server :as http]
            [reitit.ring :as ring]
            [ring.middleware.defaults
             :refer [wrap-defaults api-defaults]]
            [ring.util.http-response :as response])
  (:gen-class))

(def camel (atom {:context nil :producer nil}))

(def test-route (c/route-builder (c/from "direct:test")
                                 (c/log "test-route: inbound body is ${body} and headers are ${headers}")
                                 (c/end)))

(defn camel-route [route body & [headers]]
  (let [pd (:producer @camel)]
    (c/send-body-and-headers pd route body (or headers {}))))

(defn handle-get [req]
  (camel-route "direct:test" "hello from GET" {:x-header-msg "hello"})
  (response/ok "cool"))

(def router
  (ring/router
   ["/hello" {:get handle-get}]))

(def app (ring/ring-handler router))

(defn -main
  "I don't do a whole lot ... yet."
  [& _]
  (http/run-server
   (wrap-defaults app api-defaults) {:port 8080})
  (let [ctx (c/camel-context)
        pd (.createProducerTemplate ctx)]
    (c/add-routes ctx test-route)
    (.start ctx)
    (reset! camel {:context ctx :producer pd})
    (c/send-body-and-headers pd "direct:test" "hello world" nil)))
