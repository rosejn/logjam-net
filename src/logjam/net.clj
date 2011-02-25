(ns logjam.net
  (:use :reload
        [lamina core connections]
        [aleph object])
  (:require :reload
        [logjam.core :as log]
        [clojure.contrib.io :as io]))

(def DEFAULT-PORT 4242)

(defn- net-writer
  [host port]
  (let [con (wait-for-result (object-client {:host host :port port}) 5000)]
    (fn [chan args] (enqueue con 
                             {:channel chan 
                              :args args}))))

(defn log-client
  "Log to network log server."
  ([channel host]
   (log-client channel host DEFAULT-PORT))
  ([channel host port]
   (log-client channel host port (gensym)))
  ([channel host port chan-key]
   (log/add-writer chan-key channel
               (net-writer host port))))

(defn- log-dispatch [writer ch client-info]
  (receive-all ch
    (fn [req]
      (when req
        (writer (:channel req) (:args req))))))

(defn log-server
  "Listens on port for incoming log messages and forwards them to 
  another writer.  Returns a function that will close the server 
  when called."
  ([writer]
   (log-server writer DEFAULT-PORT))
  ([writer port]
   (start-object-server
     (partial log-dispatch writer)
     {:port port})))

