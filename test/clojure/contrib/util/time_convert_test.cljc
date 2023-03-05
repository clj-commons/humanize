(ns clojure.contrib.util.time-convert-test
  (:require
    #?(:clj  [clojure.test :refer [deftest is]]
       :cljs [cljs.test :refer-macros [deftest is]])
    [cljc.java-time.extn.predicates :as jt.predicates]
    [cljc.java-time.local-date :as jt.ld]
    [cljc.java-time.local-date-time :as jt.ldt]
    [clojure.contrib.util.time-convert :as tc]
    ;; NOTE: clj-time is included here for testing purposes, but not in the humanize library
    #?(:clj  [clj-time.core :as clj-time]
       :cljs [cljs-time.core :as clj-time]))
  (:import
    (org.joda.time Instant)))

(deftest coerce-to-local-date-time-test
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time (jt.ldt/now))))
      "java.time.LocalDateTime --> java.time.LocalDateTime")
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time (jt.ld/now))))
      "java.time.LocalDate --> java.time.LocalDateTime")
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time (clj-time/now))))
      "org.joda.time.DateTime --> java.time.LocalDateTime")
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time (Instant.))))
      "org.joda.time.Instant --> java.time.LocalDateTime")
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time (java.util.Date.))))
      "java.util.Date --> java.time.LocalDateTime")
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time "2022-01-01T13:45:30")))
      "iso8601 String --> java.time.LocalDateTime")
  (is (true? (jt.predicates/local-date-time? (tc/coerce-to-local-date-time "2022-01-01")))
      "date String --> java.time.LocalDateTime")
  (is (thrown? Exception (tc/coerce-to-local-date-time "banana")))
  (is (thrown? Exception (tc/coerce-to-local-date-time {})))
  (is (thrown? Exception (tc/coerce-to-local-date-time true)))
  (is (thrown? Exception (tc/coerce-to-local-date-time nil))))
