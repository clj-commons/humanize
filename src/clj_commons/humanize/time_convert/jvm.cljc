(ns ^:no-doc clj-commons.humanize.time-convert.jvm
  "Separate out the JVM-only checks and conversions."
  (:import (java.util Date)
           (java.time LocalDate)
           (java.text SimpleDateFormat)))

(defn java-util-date? [d]
  (instance? Date d))

(def ^:private java-util-date-iso8601-formatter
  (SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss"))

(defn java-util-date->iso8601-str
  ^String [^Date date]
  (.format java-util-date-iso8601-formatter date))

(defn java-time-local-date->iso8601-str
  ^String [^LocalDate date]
  (str (.toString date) "T00:00:00"))
