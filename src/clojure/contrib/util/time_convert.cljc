(ns clojure.contrib.util.time-convert
  (:require
    [cljc.java-time.extn.predicates :as jt.predicates]
    [cljc.java-time.format.date-time-formatter :as dt.formats]
    [cljc.java-time.local-date-time :as jt.ldt]))

(def joda-time-present?
  (try
    (import '(org.joda.time DateTime Instant))
    true
    (catch Exception _ false)))

(defn joda-time-date-time? [dt]
  (and joda-time-present?
       (instance? org.joda.time.DateTime dt)))

(defn joda-time-instant? [i]
  (and joda-time-present?
       (instance? org.joda.time.Instant i)))

(defn java-util-date? [d]
  (instance? java.util.Date d))

(def java-util-date-iso8601-formatter
  (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss"))

(defn java-util-date->iso8601-str
  ^String
  [^java.util.Date date]
  (.format java-util-date-iso8601-formatter date))

(defn java-time-local-date->iso8601-str
  ^String
  [^java.time.LocalDate date]
  (str (.toString date) "T00:00:00"))

(defn looks-like-an-iso8601-string?
  [s]
  (and (string? s)
       (boolean (re-matches #"^\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d$" s))))

(defn looks-like-a-date-string?
  [s]
  (and (string? s)
       (boolean (re-matches #"^\d\d\d\d-\d\d-\d\d$" s))))

(defn coerce-to-local-date-time
  "Does it's best to convert t into a java.time.LocalDateTime object.
  Accepts:
  - java.time.LocalDateTime and java.time.LocalDate
  - org.joda.time.DateTime and org.joda.time.Instant (if the JodaTime library is present)
  - java.util.Date
  - Strings in 'yyyy-MM-dd' and 'yyyy-MM-ddTHH:MM:SS' formats

  Throws an Exception if unable to convert."
  [t]
  (cond
    ;; t is already a java.time.LocalDateTime
    (jt.predicates/local-date-time? t) t

    ;; java.time.LocalDate
    (jt.predicates/local-date? t) (jt.ldt/parse (java-time-local-date->iso8601-str t) dt.formats/iso-date-time)

    ;; joda-time types
    (or (joda-time-date-time? t)
        (joda-time-instant? t))
    (jt.ldt/parse (.toString t) dt.formats/iso-date-time)

    ;; java.util.Date
    (java-util-date? t) (jt.ldt/parse (java-util-date->iso8601-str t) dt.formats/iso-date-time)

    ;; Strings
    (looks-like-an-iso8601-string? t) (jt.ldt/parse t dt.formats/iso-date-time)
    (looks-like-a-date-string? t) (jt.ldt/parse (str t "T00:00:00") dt.formats/iso-date-time)

    ;; ¯\_(ツ)_/¯
    :else (throw (Exception. "Unable to coerce to java.time.LocalDateTime"))))
