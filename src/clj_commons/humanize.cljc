(ns clj-commons.humanize
  (:refer-clojure :exclude [abs])
  (:require #?(:clj [clojure.math :as math :refer [floor round log log10]])
            [clj-commons.humanize.inflect :refer [pluralize-noun in?]]
            [clojure.string :as string :refer [join]]
            #?@(:clj [[cljc.java-time.duration :as jt.duration]
                      [cljc.java-time.local-date-time :as jt.ldt]]
                :cljs [[cljs.java-time.duration :as jt.duration]
                       [cljs.java-time.local-date-time :as jt.ldt]])
            [clj-commons.humanize.time-convert :refer [coerce-to-local-date-time]]
            #?@(:cljs [[goog.string :as gstring]
                       [goog.string.format]])))

#?(:clj  (def ^:private num-format format)
   :cljs (def ^:private num-format #(gstring/format %1 %2)))

#?(:clj (def ^:private expt math/pow)
   :cljs (def ^:private expt (.-pow js/Math)))
#?(:cljs (def ^:private floor (.-floor js/Math)))
#?(:cljs (def ^:private round (.-round js/Math)))
#?(:clj (def ^:private abs clojure.core/abs)
   :cljs (def ^:private abs (.-abs js/Math)))

#?(:cljs (def ^:private log (.-log js/Math)))

#?(:cljs (def ^:private rounding-const 1000000))

#?(:cljs (def ^:private log10 (or (.-log10 js/Math)         ;; prefer native implementation
                                #(/ (.round js/Math
                                      (* rounding-const
                                        (/ (.log js/Math %)
                                          js/Math.LN10)))
                                   rounding-const))))       ;; TODO: improve rounding here

#?(:clj  (def ^:private char->int #(Character/getNumericValue %))
   :cljs (def ^:private char->int #(int %)))

(defn intcomma
  "Converts an integer to a string containing commas. every three digits.
   For example, 3000 becomes '3,000' and 45000 becomes '45,000'. "
  [num]
  (let [decimal     (abs (int num))                         ;;  FIXME: (abs )
        sign        (if (< num 0) "-" "")

        ;; convert into string representation
        repr        (str decimal)
        repr-len    (count repr)

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
  (let [ordinals      ["th", "st", "nd", "rd", "th",
                       "th", "th", "th", "th", "th"]
        remainder-100 (rem num 100)
        remainder-10  (rem num 10)]

    (if (in? remainder-100 [11 12 13])
      ;; special case for *11, *12, *13
      (str num (ordinals 0))
      (str num (ordinals remainder-10)))))

(defn- logn [num base]
  (/ (round (log num))
    (round (log base))))


(def ^:private human-pows [[100 " googol"]
                           [33 " decillion"]
                           [30 " nonillion"]
                           [27 " octillion"]
                           [24 " septillion"]
                           [21 " sextillion"]
                           [18 " quintillion"]
                           [15 " quadrillion"]
                           [12 " trillion"]
                           [9 " billion"]
                           [6 " million"]
                           [0 ""]])

(defn intword
  "Converts a large integer to a friendly text representation. Works best for
   numbers over 1 million. For example, 1000000 becomes '1.0 million', 1200000
   becomes '1.2 million' and '1200000000' becomes '1.2 billion'.  Supports up to
   decillion (33 digits) and googol (100 digits)."
  [num & {:keys [format] :or {format "%.1f"}}]
  (let [base-pow (int (floor (log10 num)))
        [base-pow suffix] (first (filter (fn [[base _]] (>= base-pow base)) human-pows))
        value    (float (/ num (expt 10 base-pow)))]
    (str (num-format format value) suffix)))

(def ^:private numap
  {0  ""
   1  "one"
   2  "two"
   3  "three"
   4  "four"
   5  "five"
   6  "six"
   7  "seven"
   8  "eight"
   9  "nine"
   10 "ten"
   11 "eleven"
   12 "twelve"
   13 "thirteen"
   14 "fourteen"
   15 "fifteen"
   16 "sixteen"
   17 "seventeen"
   18 "eighteen"
   19 "nineteen"
   20 "twenty"
   30 "thirty"
   40 "forty"
   50 "fifty"
   60 "sixty"
   70 "seventy"
   80 "eighty"
   90 "ninety"})

(defn numberword
  "Takes a number and return a full written string form. For example,
   23237897 will be written as \"twenty-three million two hundred and
   thirty-seven thousand eight hundred and ninety-seven\".  "
  [num]

  ;; special case for zero
  (if (zero? num)
    "zero"

    (let [digitcnt   (int (log10 num))
          divisible? (fn [num div] (zero? (rem num div)))
          n-digit    (fn [num n] (char->int (.charAt (str num) n)))] ;; TODO rename

      (cond
        ;; handle million part
        (>= digitcnt 6) (if (divisible? num 1000000)
                          (join " " [(numberword (int (/ num 1000000)))
                                     "million"])
                          (join " " [(numberword (int (/ num 1000000)))
                                     "million"
                                     (numberword (rem num 1000000))]))

        ;; handle thousand part
        (>= digitcnt 3) (if (divisible? num 1000)
                          (join " " [(numberword (int (/ num 1000)))
                                     "thousand"])
                          (join " " [(numberword (int (/ num 1000)))
                                     "thousand"
                                     (numberword (rem num 1000))]))

        ;; handle hundred part
        (>= digitcnt 2) (if (divisible? num 100)
                          (join " " [(numap (int (/ num 100)))
                                     "hundred"])
                          (join " " [(numap (int (/ num 100)))
                                     "hundred"
                                     "and"
                                     (numberword (rem num 100))]))

        ;; handle the last two digits
        (< num 20) (numap num)
        (divisible? num 10) (numap num)
        :else (join "-" [(numap (* 10 (n-digit num 0)))
                         (numap (n-digit num 1))])))))

(def ^:private decimal-sizes [:B :KB :MB :GB :TB :PB :EB :ZB :YB])

(def ^:private binary-sizes [:B :KiB :MiB :GiB :TiB :PiB :EiB :ZiB :YiB])

(defn filesize
  "Format a number of bytes as a human readable filesize (eg. 10 kB). By
   default, decimal suffixes (kB, MB) are used.  Passing :binary true will use
   binary suffixes (KiB, MiB) instead."
  [bytes & {:keys [binary format]
            :or   {binary false
                   format "%.1f"}}]

  (if (zero? bytes)
    ;; special case for zero
    "0"

    (let [units    (if binary binary-sizes decimal-sizes)
          base     (if binary 1024 1000)

          base-pow (int (floor (logn bytes base)))
          ;; if base power shouldn't be larger than biggest unit
          base-pow (if (< base-pow (count units))
                     base-pow
                     (dec (count units)))
          suffix   (name (get units base-pow))
          value    (float (/ bytes (expt base base-pow)))]
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
   (truncate string length "…")))

(defn oxford
  "Converts a list of items to a human-readable string, such as \"apple, pear, and 2 other fruits\".

  Options:
  :maximum-display - the maximum number of items to display before identifying the remaining count (default: 4)
  :truncate-noun - the string used to identify the type of items in the list, e.g., \"fruit\" - will
  be pluralized if necessary
  :number-format - function used to format the number of additional items in the list (default: `str`)
  "
  [coll & {:keys [maximum-display truncate-noun number-format]
           :or   {maximum-display 4
                  number-format   str}}]

  (let [coll-length (count coll)]
    (cond
      ;; if coll has one or zero items
      (< coll-length 2) (join coll)

      ;; if coll has exactly two items, there won't be a comma, so join them with "and"
      (and (= coll-length 2)
        (<= coll-length maximum-display))
      (str (first coll) " and " (second coll))

      ;; if the number of items doesn't exceed maximum display size
      (<= coll-length maximum-display) (let [before-last (take (dec coll-length) coll)
                                             last-item   (last coll)]
                                         (str (join (interpose ", " before-last))
                                              ", and " last-item))

      (> coll-length maximum-display) (let [display-coll (take maximum-display coll)
                                            remaining    (- coll-length maximum-display)
                                            remaining'   (number-format remaining)
                                            last-item    (if (string/blank? truncate-noun)
                                                           (str remaining' " " (pluralize-noun remaining "other"))
                                                           (str remaining' " other " (pluralize-noun remaining
                                                                                       truncate-noun)))]
                                        (str
                                          (join (interpose ", " display-coll))
                                          ; if only one item is displayed there should be no oxford comma
                                          (when-not (= 1 maximum-display)
                                            ",")
                                          " and " last-item))
      ;; TODO: shouldn't reach here, throw exception
      :else coll-length)))

(def ^:private one-minute-in-seconds 60)
(def ^:private one-hour-in-seconds (* 60 one-minute-in-seconds))
(def ^:private one-day-in-seconds (* 24 one-hour-in-seconds))
(def ^:private one-week-in-seconds (* 7 one-day-in-seconds))
(def ^:private one-month-in-seconds (* 4 one-week-in-seconds))
(def ^:private one-year-in-seconds (* 52 one-week-in-seconds))
(def ^:private one-decade-in-seconds (* 10 one-year-in-seconds))
(def ^:private one-century-in-seconds (* 100 one-year-in-seconds))
(def ^:private one-millennia-in-seconds (* 1000 one-year-in-seconds))

(defn format-delta-str
  [amount time-unit suffix prefix future-time?]
  (if future-time?
    (str prefix " " amount " " (pluralize-noun amount time-unit))
    (str amount " " (pluralize-noun amount time-unit) " " suffix)))

(defn datetime
  "Given a java.time.LocalDate or java.time.LocalDateTime, returns a
  human-friendly representation of the amount of time elapsed compared to now.

   Optional keyword args:
  * :now-dt - specify the value for 'now'
  * :prefix - adjust the verbiage for times in the future
  * :suffix - adjust the verbiage for times in the past"
  [then-dt & {:keys [now-dt suffix prefix]
              :or   {now-dt (jt.ldt/now)
                     suffix "ago"
                     prefix "in"}}]
  (let [then-dt            (coerce-to-local-date-time then-dt)
        now-dt             (coerce-to-local-date-time now-dt)
        future-time?       (jt.ldt/is-after then-dt now-dt)
        ;; get the Duration between the two times
        time-between       (-> (jt.duration/between then-dt now-dt)
                             (jt.duration/abs))
        delta-in-seconds   (jt.duration/get-seconds time-between)
        delta-in-minutes   (int (/ delta-in-seconds one-minute-in-seconds))
        delta-in-hours     (int (/ delta-in-seconds one-hour-in-seconds))
        delta-in-days      (int (/ delta-in-seconds one-day-in-seconds))
        delta-in-weeks     (int (/ delta-in-seconds one-week-in-seconds))
        delta-in-months    (int (/ delta-in-seconds one-month-in-seconds))
        delta-in-years     (int (/ delta-in-seconds one-year-in-seconds))
        delta-in-decades   (int (/ delta-in-seconds one-decade-in-seconds))
        delta-in-centuries (int (/ delta-in-seconds one-century-in-seconds))
        delta-in-millennia (int (/ delta-in-seconds one-millennia-in-seconds))]
    (cond
      (pos? delta-in-millennia)
      (format-delta-str delta-in-millennia "millenium" suffix prefix future-time?)

      (pos? delta-in-centuries)
      (format-delta-str delta-in-centuries "century" suffix prefix future-time?)

      (pos? delta-in-decades)
      (format-delta-str delta-in-decades "decade" suffix prefix future-time?)

      (pos? delta-in-years)
      (format-delta-str delta-in-years "year" suffix prefix future-time?)

      (pos? delta-in-months)
      (format-delta-str delta-in-months "month" suffix prefix future-time?)

      (pos? delta-in-weeks)
      (format-delta-str delta-in-weeks "week" suffix prefix future-time?)

      (pos? delta-in-days)
      (format-delta-str delta-in-days "day" suffix prefix future-time?)

      (pos? delta-in-hours)
      (format-delta-str delta-in-hours "hour" suffix prefix future-time?)

      (pos? delta-in-minutes)
      (format-delta-str delta-in-minutes "minute" suffix prefix future-time?)

      (pos? delta-in-seconds)
      (format-delta-str delta-in-seconds "second" suffix prefix future-time?)

      future-time?
      (str prefix " a moment")

      :else
      (str "a moment " suffix))))

(def ^:private duration-periods
  [[(* 1000 60 60 24 365) "year"]
   [(* 1000 60 60 24 31) "month"]
   [(* 1000 60 60 24 7) "week"]
   [(* 1000 60 60 24) "day"]
   [(* 1000 60 60) "hour"]
   [(* 1000 60) "minute"]
   [1000 "second"]])

(defn- duration-terms
  "Converts a duration, in milliseconds, to a set of terms describing the duration.
  The terms are in descending order, largest period to smallest.

  Each term is a tuple of count and period name, e.g., `[5 \"second\"]`.

  After seconds are accounted for, remaining milliseconds are ignored."
  [duration-ms]
  {:pre [(<= 0 duration-ms)]}
  (loop [remainder duration-ms
         [[period-ms period-name] & more-periods] duration-periods
         terms     []]
    (cond
      (nil? period-ms)
      terms

      (< remainder period-ms)
      (recur remainder more-periods terms)

      :else
      (let [period-count   (int (/ remainder period-ms))
            next-remainder (mod remainder period-ms)]
        (recur next-remainder more-periods
          (conj terms [period-count period-name]))))))

(defn duration
  "Converts duration, in milliseconds, into a string describing it in terms
  of years, months, weeks, days, hours, minutes, and seconds.

  Ex:

     (duration 325100) => \"five minutes, twenty-five seconds\"

  The months and years periods are not based on actual calendar, so are approximate; this
  function works best for shorter periods of time.

  The optional options map allow some control over the result.

  :list-format (default: a function) can be set to a function such as oxford

  :number-format (default: numberword) function used to format period counts

  :short-text (default: \"less than a second\") "
  {:added "0.2.1"}
  ([duration-ms]
   (duration duration-ms nil))
  ([duration-ms options]
   (let [terms (duration-terms duration-ms)
         {:keys [number-format list-format short-text]
          :or   {number-format numberword
                 short-text    "less than a second"
                 ;; This default, instead of oxford, because the entire string is a single "value"
                 list-format   #(join ", " %)}} options]
     (if (seq terms)
       (->> terms
         (map (fn [[period-count period-name]]
                (str (number-format period-count)
                     " "
                     (pluralize-noun period-count period-name))))
         list-format)
       short-text))))
