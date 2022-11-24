(ns redpill-linpro.camelot
  (:require [clj-camel.core :as c]
            [clj-camel.util :as u]
            [clojure.tools.macro :refer [symbol-macrolet]]
            [org.httpkit.server :as http]
            [reitit.ring :as ring]
            [redpill-linpro.camelot.stables :as stables]
            [redpill-linpro.camelot.routes :as camel-routes]
            [ring.middleware.reload]
            [ring.middleware.defaults
             :refer [wrap-defaults api-defaults]]
            [ring.util.http-response :as response])
  (:gen-class))

; reitit + ring HTTP handling
(defn handle-get [req]
  (let [result (stables/camel-route :direct/test "hello from GET")]
    (response/ok result)))

(def router
  (ring/router
   ["/hello" {:get handle-get}]))
(def app (ring/ring-handler router))
(def reloadable-app (ring.middleware.reload/wrap-reload #'app))


; let's go
(defn -main
  "I don't do a whole lot ... yet."
  [& _]
  (http/run-server
   (wrap-defaults #'reloadable-app api-defaults) {:port 8080 :join? false})
  (let [ctx (c/camel-context)
        pd (.createProducerTemplate ctx)
        cs (.createConsumerTemplate ctx)]
    (reset! stables/camel {:context ctx :producer pd :consumer cs})
    (stables/create-routes ctx camel-routes/test-route) ;;we should automate this
    (stables/bind-interop)
    (.start ctx)
    (.start cs)
    (.requestBody pd "direct:test" "hello")))
