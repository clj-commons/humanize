(ns clojure.contrib.humanize)

(defn in? [x coll]
  """ Return true if x is in coll, else false. """
  (if (empty? coll)
    false
    (if (= x (first coll))
      true
      (in? x (rest coll)))))


(defn intcomma [num]
  """ Converts an integer to a string containing commas. every three digits.
      For example, 3000 becomes '3,000' and 45000 becomes '45,000'. """
  (let [;; convert into string representation
        repr (str num)
        repr-len (count repr)

        ;; right-aligned 3 elements partition
        partitioned [(subs repr 0 (rem repr-len 3))
                     (map #(apply str %)
                          (partition 3 (subs repr
                                             (rem repr-len 3))))]

        ;; flatten
        partitioned (flatten partitioned)]

    (if (in? (first partitioned) ["" "-"])
      ;; skip the first if it is negative sign or empty
      (apply str (first partitioned)
             (interpose "," (rest partitioned)))
      ;; else interpose all
      (apply str (interpose "," partitioned)))))


(defn ordinal [num]
  """Converts an integer to its ordinal as a string. 1 is '1st', 2 is '2nd',
    3 is '3rd', etc."""
    (let [ordinals ["th", "st", "nd", "rd", "th",
                    "th", "th", "th", "th", "th"]]

      (if (in? (rem num 100) [11 12 13])
        ;; special case for *11, *12, *13
        (str num (ordinals 0))
        (str num (ordinals (rem num 10))))))
