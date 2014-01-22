(ns clojure.contrib.humanize
  (:require [clojure.math.numeric-tower :refer :all]))

(defn in? [x coll]
  """ Return true if x is in coll, else false. """
  (some #(= x %) coll))

(defn intcomma [num]
  """ Converts an integer to a string containing commas. every three digits.
      For example, 3000 becomes '3,000' and 45000 becomes '45,000'. """
  (let [
        decimal (abs (int num)) ;;  FIXME: (abs )
        sign (if (< num 0) "-" "")

        ;; convert into string representation
        repr (str decimal)
        repr-len (count repr)

        ;; right-aligned 3 elements partition
        partitioned [(subs repr 0 (rem repr-len 3))
                     (map #(apply str %)
                          (partition 3 (subs repr
                                             (rem repr-len 3))))]

        ;; flatten, and remove empty string
        partitioned (remove empty? (flatten partitioned))]

    (apply str sign (interpose "," partitioned))))


(defn ordinal [num]
  """Converts an integer to its ordinal as a string. 1 is '1st', 2 is '2nd',
    3 is '3rd', etc."""
    (let [ordinals ["th", "st", "nd", "rd", "th",
                    "th", "th", "th", "th", "th"]
          remainder-100 (rem num 100)
          remainder-10  (rem num 10)]

      (if (in? remainder-100 [11 12 13])
        ;; special case for *11, *12, *13
        (str num (ordinals 0))
        (str num (ordinals remainder-10)))))

(defn- logn [num base]
  (/ (java.lang.Math/log num)
     (java.lang.Math/log base)))

(def decimal-sizes  [:B, :KB, :MB, :GB, :TB, :PB, :EB, :ZB, :YB])
(def binary-sizes [:B, :KiB, :MiB, :GiB, :TiB, :PiB, :EiB, :ZiB, :YiB])

(defn filesize [bytes &
                {:keys [binary format]
                 :or {binary false
                      format "%.1f"
                      }}]
  (let [units (if binary binary-sizes decimal-sizes)
        base  (if binary 1024 1000)

        base-pow  (int (floor (logn bytes base)))
        base-pow  (if (< base-pow (count units))
                    base-pow
                    (dec (count units)))
        suffix (name (get units base-pow))
        value (float (/ bytes (expt base base-pow)))
        ]

    (clojure.core/format (str format "%s") value suffix)))
