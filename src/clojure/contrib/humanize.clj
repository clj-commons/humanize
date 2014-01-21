(ns clojure.contrib.humanize)

(defn intcomma [num]
  """ Converts an integer to a string containing commas.
      every three digits. For example, 3000 becomes '3,000'
      and 45000 becomes '45,000'. """

  (let [;; convert into string representation
        repr (str num)
        repr-len (count repr)

        ;; right-aligned 3 elements partition
        partitioned [(subs repr 0 (rem repr-len 3))
                     (map #(apply str %)
                          (partition 3 (subs repr
                                             (rem repr-len 3))))]

        ;; remove empty "", and flatten
        partitioned (flatten (remove empty? partitioned))]

    (if (= (first partitioned) "-")
      ;; skip the first if it is only negative sign
      (apply str (first partitioned)
             (interpose "," (rest partitioned)))
      ;; interpose the rest with comma
      (apply str (interpose "," partitioned)))))
