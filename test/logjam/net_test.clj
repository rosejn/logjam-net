(ns logjam.net-test
  (:use clojure.test
        clojure.stacktrace
;        clojure.contrib.repl-utils
        logjam.net
        [clojure.java.io :only (reader)])
  (:require [clojure.contrib.io :as io]
            [logjam.core :as log]))

(log/channel :b :a)
(log/channel :c :b)

(def test-file "test/server.log")

(deftest basic-net-test
  (let [s (log-server (log/file-writer test-file))]
    (try
      (log-client :b "localhost")
      (log/to :a "a")
      (log/to :b "b stuff")
      (log/to :c "c message")
      (Thread/sleep 200)
    (let [lines (line-seq (reader test-file))
          b (first lines)
          c (second lines)]
      (is (= b "[b] b stuff"))
      (is (= c "[c] c message")))
    (finally
      (io/delete-file test-file)
      (s)))))
