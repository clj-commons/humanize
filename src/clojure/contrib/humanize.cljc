(ns clojure.contrib.humanize
  (:require #?(:clj  [clojure.math.numeric-tower :refer [expt floor round abs]])
            [clojure.contrib.inflect :refer [pluralize-noun in?]]
            [clojure.string :refer [join]]
            #?(:clj  [clj-time.core  :refer [date-time interval in-seconds
                                             in-minutes in-hours in-days
                                             in-weeks in-months in-years]]
               :cljs [cljs-time.core :refer [date-time interval in-seconds
                                             in-minutes in-hours in-days
                                             in-weeks in-months in-years]])
            #?(:cljs [goog.string :as gstring])
            #?(:cljs [goog.string.format])
            #?(:clj  [clj-time.local  :refer [local-now]]
               :cljs [cljs-time.local :refer [local-now]])
            #?(:clj  [clj-time.coerce  :refer [to-date-time to-string]]
               :cljs [cljs-time.coerce :refer [to-date-time to-string]])))

#?(:clj  (def num-format format)
   :cljs (def num-format #(gstring/format %1 %2)))

#?(:cljs (def expt (.-pow js/Math)))
#?(:cljs (def floor (.-floor js/Math)))
#?(:cljs (def round (.-round js/Math)))
#?(:cljs (def abs (.-abs js/Math)))

#?(:clj (def log #(java.lang.Math/log %))
   :cljs (def log (.-log js/Math)))

#?(:clj (def log10 #(java.lang.Math/log10 %))
   :cljs (def log10 #(/ (.round js/Math (* 100000 (/ (.log js/Math %) js/Math.LN10)))
                        100000)))                           ;; FIXME implement proper rounding

#?(:clj (def char->int #(Character/getNumericValue %))
   :cljs (def char->int #(int %)))

(defn intcomma
  "Converts an integer to a string containing commas. every three digits.
   For example, 3000 becomes '3,000' and 45000 becomes '45,000'. "
  [num]
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


(defn ordinal
  "Converts an integer to its ordinal as a string. 1 is '1st', 2 is '2nd',
   3 is '3rd', etc."
  [num]
    (let [ordinals ["th", "st", "nd", "rd", "th",
                    "th", "th", "th", "th", "th"]
          remainder-100 (rem num 100)
          remainder-10  (rem num 10)]

      (if (in? remainder-100 [11 12 13])
        ;; special case for *11, *12, *13
        (str num (ordinals 0))
        (str num (ordinals remainder-10)))))

(defn logn [num base]
  (/ (round (log num))
     (round (log base))))

(defn intword
  "Converts a large integer to a friendly text representation. Works best for
   numbers over 1 million. For example, 1000000 becomes '1.0 million', 1200000
   becomes '1.2 million' and '1200000000' becomes '1.2 billion'.  Supports up to
   decillion (33 digits) and googol (100 digits)."
  [num & {:keys [format] :or {format "%.1f"}}]
  (let [human-pows [[0 ""]
                    [6 " million"]
                    [9 " billion"]
                    [12 " trillion"]
                    [15 " quadrillion"]
                    [18 " quintillion"]
                    [21 " sextillion"]
                    [24 " septillion"]
                    [27 " octillion"]
                    [30 " nonillion"]
                    [33 " decillion"]
                    [100 " googol"]]
        base-pow  (int (floor (log10 num)))
        [base-pow suffix] (first (filter (fn [[base _]] (>= base-pow base)) (reverse human-pows)))
        value (float (/ num (expt 10 base-pow)))]
    (str (num-format format value) suffix)))


(def ^:private numap
  {0 "",1 "one",2 "two",3 "three",4 "four",5 "five",
   6 "six",7 "seven",8 "eight",9 "nine",10 "ten",
   11 "eleven",12 "twelve",13 "thirteen",14 "fourteen",
   15 "fifteen",16 "sixteen",17 "seventeen",18 "eighteen",
   19 "nineteen",20 "twenty",30 "thirty",40 "forty",
   50 "fifty",60 "sixty",70 "seventy",80 "eighty",90 "ninety"})

(defn numberword
  "Takes a number and return a full written string form. For example,
   23237897 will be written as \"twenty-three million two hundred and
   thirty-seven thousand eight hundred and ninety-seven\".  "
  [num]

  ;; special case for zero
  (if (zero? num)
    "zero"

  (let [digitcnt (int (log10 num))
        divisible? (fn [num div] (zero? (rem num div)))
        n-digit (fn [num n] (char->int (.charAt (str num) n)))] ;; TODO rename

    (cond
     ;; handle million part
     (>= digitcnt 6)    (join " " [(numberword (int (/ num 1000000)))
                                  "million"
                                  (numberword (rem num 1000000))])

     ;; handle thousand part
     (>= digitcnt 3)    (join " " [(numberword (int (/ num 1000)))
                                   "thousand"
                                   (numberword (rem num 1000))])

     ;; handle hundred part
     (>= digitcnt 2)    (if (divisible? num 100)
                          (join " " [(numap (int (/ num 100)))
                                     "hundred"])
                          (join " " [(numap (int (/ num 100)))
                                     "hundred"
                                     "and"
                                     (numberword (rem num 100))]))

     ;; handle the last two digits
     (< num 20)                 (numap num)
     (divisible? num 10)        (numap num)
     :else                      (join "-" [(numap (* 10 (n-digit num 0)))
                                           (numap (n-digit num 1))])))))

(defn filesize
  "Format a number of bytes as a human readable filesize (eg. 10 kB). By
   default, decimal suffixes (kB, MB) are used.  Passing :binary true will use
   binary suffixes (KiB, MiB) instead."
  [bytes & {:keys [binary format]
            :or {binary false
                 format "%.1f"}}]

  (if (zero? bytes)
    ;; special case for zero
    "0"

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

    (str (num-format format value) suffix))))

(defn truncate
  "Truncate a string with suffix (ellipsis by default) if it is
   longer than specified length."

  ([string length suffix]
     (let [string-len (count string)
           suffix-len (count suffix)]

       (if (<= string-len length)
         string
         (str (subs string 0 (- length suffix-len)) suffix))))

  ([string length]
     (truncate string length "...")))

(defn oxford
  "Converts a list of items to a human readable string
   with an optional limit."
  [coll  & {:keys [maximum-display truncate-noun]
            :or {maximum-display 4
                 truncate-noun nil}}]

  (let [coll-length (count coll)]
    (cond
     ;; if coll has one or zero items
     (< coll-length 2) (join coll)

     ;; if the number of items doesn't exceed maximum display size
     (<= coll-length maximum-display) (let [before-last (take (dec coll-length) coll)
                                            last-item (last coll)]
                                        (str (join (interpose ", " before-last))
                                             ", and " last-item))

     (> coll-length maximum-display) (let [display-coll (take maximum-display coll)
                                           remaining (- coll-length maximum-display)
                                           last-item (if (empty? truncate-noun)
                                                       (str remaining " " (pluralize-noun remaining "other"))
                                                       (str remaining " other " (pluralize-noun remaining
                                                                                                truncate-noun)))
                                           ]
                                       (str (join (interpose ", " display-coll))
                                            ", and " last-item))

     ;; TODO: shouldn't reach here, throw exception
     :else coll-length)))

(defn datetime
  "Given a datetime or date, return a human-friendly representation
   of the amount of time elapsed. "
  [then-dt & {:keys [now-dt suffix]
              :or {now-dt (local-now)
                   suffix  "ago"}}]
  (let [then-dt (to-date-time then-dt)
        now-dt  (to-date-time now-dt)
        diff    (interval then-dt now-dt)]
    (cond
     ;; if the diff is less than a second
     (<= (in-seconds diff) 0) (str "a moment " suffix)

     ;; if the diff is less than a minute
     (<= (in-minutes diff) 0) (str (in-seconds diff) " "
                                   (pluralize-noun (in-seconds diff) "second")
                                   " " suffix)

     ;; if the diff is less than an hour
     (<= (in-hours diff) 0) (str (in-minutes diff) " "
                                 (pluralize-noun (in-minutes diff) "minute")
                                 " " suffix)

     ;; if the diff is less than a day
     (<= (in-days diff) 0) (str (in-hours diff) " "
                                (pluralize-noun (in-hours diff) "hour")
                                " " suffix)

     ;; if the diff is less than a week
     (<= (in-weeks diff) 0) (str (in-days diff) " "
                                 (pluralize-noun (in-days diff) "day")
                                 " " suffix)

     ;; if the diff is less than a month
     (<= (in-months diff) 0) (str (in-weeks diff) " "
                                  (pluralize-noun (in-weeks diff) "week")
                                  " " suffix)

     ;; if the diff is less than a year
     (<= (in-years diff) 0) (str (in-months diff) " "
                                 (pluralize-noun (in-months diff) "month")
                                 " " suffix)

     ;; if the diff is less than a decade
     (< (in-years diff) 10) (str (in-years diff) " "
                                 (pluralize-noun (in-years diff) "year")
                                 " " suffix)

     ;; if the diff is less than a century
     (< (in-years diff) 100) (str (-> diff in-years (/ 10) long) " "
                                   (pluralize-noun (-> diff in-years (/ 10) long) "decade")
                                   " " suffix)

     ;; if the diff is less than a millennium
     (< (in-years diff) 1000) (str (-> diff in-years (/ 100) long) " "
                                    (pluralize-noun (-> diff in-years (/ 100) long) "century")
                                    " " suffix)

     ;; if the diff is less than 10 millennia
     (< (in-years diff) 10000) (str (-> diff in-years (/ 1000) long) " "
                                    (pluralize-noun (-> diff in-years (/ 1000) long) "millennium")
                                    " " suffix)

     ;; FIXME:
     :else (to-string diff))))
