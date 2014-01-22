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

(defn logn [num base]
  (/ (round (java.lang.Math/log num))
     (round (java.lang.Math/log base))))

(defn intword [num & {:keys [format] :or {format "%.1f"}}]
  """Converts a large integer to a friendly text representation. Works best for
    numbers over 1 million. For example, 1000000 becomes '1.0 million', 1200000
    becomes '1.2 million' and '1200000000' becomes '1.2 billion'.  Supports up to
    decillion (33 digits) and googol (100 digits)."""
  (let [human-pows {
                    0 "",
                      6 " million",
                      9  " billion",
                      12  " trillion",
                      15  " quadrillion",
                      18 " quintillion",
                      21  " sextillion",
                      24  " septillion",
                      27 " octillion",
                      30  " nonillion",
                      33  " decillion",
                      100 " googol",
                      }

        base-pow  (int (floor (java.lang.Math/log10 num)))
        base-pow  (if (contains? human-pows base-pow)
                    base-pow
                    (last (remove #(> % base-pow) (sort (keys human-pows)))))

        suffix (get human-pows base-pow)
        value (float (/ num (expt 10 base-pow)))
        ]
    (clojure.core/format (str format "%s") value suffix)
    ))


(defn filesize [bytes & {:keys [binary format]
                         :or {binary false
                              format "%.1f"}}]
  """ Format a number of byteslike a human readable filesize (eg. 10 kB).  By
    default, decimal suffixes (kB, MB) are used.  Passing binary=true will use
    binary suffixes (KiB, MiB) are used"""

  (let [decimal-sizes  [:B, :KB, :MB, :GB, :TB,
                        :PB, :EB, :ZB, :YB]
        binary-sizes [:B, :KiB, :MiB, :GiB, :TiB,
                      :PiB, :EiB, :ZiB, :YiB]

        units (if binary binary-sizes decimal-sizes)
        base  (if binary 1024 1000)

        base-pow  (int (floor (logn bytes base)))
        ;; if base power shouldn't be larger than biggest unit
        base-pow  (if (< base-pow (count units))
                    base-pow
                    (dec (count units)))
        suffix (name (get units base-pow))
        value (float (/ bytes (expt base base-pow)))
        ]

    (clojure.core/format (str format "%s") value suffix)))
