#_:clj-kondo/ignore
(ns ^:no-doc clj-commons.humanize.time-convert
  "Internal utility to convert strings and other typs into LocalDateTime "
  (:require [cljc.java-time.extn.predicates :as jt.predicates]
            [cljc.java-time.format.date-time-formatter :as dt.formats]
            [cljc.java-time.local-date-time :as jt.ldt]
            [cljc.java-time.instant :as jt.i]
            [cljc.java-time.zone-id :as jt.zi]
            #?(:clj [clj-commons.humanize.time-convert.jvm :as jvm])))

(defn- looks-like-an-iso8601-string?
  [s]
  (and (string? s)
    (boolean (re-matches #"^\d\d\d\d-\d\d-\d\dT\d\d:\d\d:\d\d$" s))))

(defn- looks-like-a-date-string?
  [s]
  (and (string? s)
    (boolean (re-matches #"^\d\d\d\d-\d\d-\d\d$" s))))

(defn coerce-to-local-date-time
  "Does its best to convert t into a java.time.LocalDateTime object.
  Accepts:
  - java.time.LocalDateTime and java.time.LocalDate
  - java.util.Date (on the JVM)
  - Strings in 'yyyy-MM-dd' and 'yyyy-MM-ddTHH:MM:SS' formats
  - js/Date

  Throws an Exception if unable to convert."
  [t]
  (cond
    ;; t is already a java.time.LocalDateTime
    (jt.predicates/local-date-time? t) t

    #?@(:clj  [(jt.predicates/local-date? t)
               (jt.ldt/parse (jvm/java-time-local-date->iso8601-str t) dt.formats/iso-date-time)

               (jvm/java-util-date? t)
               (jt.ldt/parse (jvm/java-util-date->iso8601-str t) dt.formats/iso-date-time)]
        :cljs [(instance? js/Date t)
               (jt.ldt/of-instant (jt.i/of-epoch-milli (.getTime t)) (jt.zi/system-default))])

    ;; Strings
    (looks-like-an-iso8601-string? t)
    (jt.ldt/parse t dt.formats/iso-date-time)

    (looks-like-a-date-string? t)
    (jt.ldt/parse (str t "T00:00:00") dt.formats/iso-date-time)

    ;; ¯\_(ツ)_/¯
    :else
    (throw (ex-info "unable to coerce to java.time.LocalDateTime"
             {:value t}))))
