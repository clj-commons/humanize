(ns clj-commons.humanize-test
  (:require #?(:clj  [clojure.test :refer [deftest testing is are]]
               :cljs [cljs.test :refer-macros [deftest testing is are]])
            [clj-commons.humanize :refer [intcomma ordinal intword numberword
                                          filesize truncate oxford datetime
                                          duration]
             :as h]
            #?@(:clj [[clojure.math :as math]
                      [cljc.java-time.local-date-time :as jt.ldt]])
            #?(:cljs [cljs.java-time.local-date-time :as jt.ldt])))

#?(:clj  (def ^:private expt math/pow)
   :cljs (def ^:private expt (.-pow js/Math)))

(deftest intcomma-test
  (are [input expected] (= expected (intcomma input))
                        100, "100"
                        1000 "1,000",
                        10123 "10,123"
                        10311 "10,311"
                        1000000 "1,000,000"
                        -100 "-100"
                        -10123 "-10,123"
                        -10311 "-10,311",
                        -1000000 "-1,000,000"))

(deftest ordinal-test
  (are [input expected] (= expected (ordinal input))
                        1 "1st"
                        2 "2nd"
                        3 "3rd"
                        4 "4th"
                        11 "11th"
                        12 "12th"
                        13 "13th"
                        101 "101st"
                        102 "102nd"
                        103 "103rd"
                        111 "111th"))

(deftest intword-test
  (are [input format expected] (= expected (if format
                                             (intword input :format format)
                                             (intword input)))
                               100 nil "100.0"
                               1000000 nil "1.0 million"
                               1200000 nil "1.2 million"
                               1290000 nil "1.3 million"
                               1000000000 nil "1.0 billion"
                               2000000000 nil "2.0 billion"
                               6000000000000 nil "6.0 trillion"
                               1300000000000000 nil "1.3 quadrillion"
                               3500000000000000000000 nil "3.5 sextillion"
                               8100000000000000000000000000000000 nil "8.1 decillion"
                               1230000 "%.2f" "1.23 million"
                               (expt 10 101) nil "10.0 googol"))

(deftest numberword-test
  (are [input expected] (= expected (numberword input))
                        0 "zero"
                        7 "seven"
                        12 "twelve"
                        40 "forty"
                        94 "ninety-four"
                        100 "one hundred"
                        51 "fifty-one"
                        234 "two hundred and thirty-four"
                        1000 "one thousand"
                        3567 "three thousand five hundred and sixty-seven"
                        44120 "forty-four thousand one hundred and twenty"
                        25223 "twenty-five thousand two hundred and twenty-three"
                        5223 "five thousand two hundred and twenty-three"
                        1000000 "one million"
                        23237897 "twenty-three million two hundred and thirty-seven thousand eight hundred and ninety-seven"))

(deftest filesize-test
  (let [f (fn [input binary format]
            (filesize input
              (cond-> {}
                binary (assoc :binary true)
                format (assoc :format format))))]
    (are [input binary format expected] (is (= expected (f input binary format)))
                                        0 nil nil "0"
                                        300 nil nil "300.0B"
                                        3000 nil nil "3.0KB"
                                        3000000 nil nil "3.0MB"
                                        3005000 nil "%.3f" "3.005MB"
                                        3000000000 nil nil "3.0GB"
                                        3000000000000 nil nil "3.0TB"
                                        3000 true nil "2.9KiB"
                                        3000000 true nil "2.9MiB"
                                        (* (expt 10 26) 30) nil nil "3000.0YB"
                                        (* (expt 10 26) 30) true nil "2481.5YiB")))

(deftest truncate-test
  (testing "truncate should not return a string larger than the given length."
    (let [string "asdfghjkl"]
      (is (= (count (truncate string 7)) 7))
      (is (= (count (truncate string 7 "1234")) 7))
      (is (= (count (truncate string 100)) (count string)))))

  (testing "testing truncate with expected data."
    (let [string "abcdefghijklmnopqrstuvwxyz"]
      ;; Strng is truncated to a total of 14 including the (default) suffix:
      (is (= (truncate string 14) "abcdefghijklm…"))
      (is (= (truncate string 14 "...kidding") "abcd...kidding")))))

(deftest oxford-test
  (let [items ["apple", "orange" "banana" "pear" "pineapple" "strawberry"]]
    (testing "should return an empty string when given an empty list."
      (is (= "" (oxford []))))

    (testing "should return a string version of a list that has only one value."
      (is (= (items 0)
            (oxford [(items 0)]))))

    (testing "should return a string with no commas & items separated with `and` when passed exactly two values in list"
      (is (= "apple and orange"
            (oxford (take 2 items)))))

    (testing "should return items separated by `and' when given a list of values"
      (is (= "apple, orange, and banana"
            (oxford (take 3 items))))
      (is (= "apple, orange, banana, and pear"
            (oxford (take 4 items)))))

    (testing "should truncate a large list of items with proper pluralization"
      (is (= "apple, orange, banana, pear, and 1 other"
            (oxford (take 5 items))))

      (is (= "apple, orange, and 3 others"
            (oxford (take 5 items)
              :maximum-display 2)))
      (is (= "apple and 3 others"
            (oxford (take 4 items)
              :maximum-display 1))))

    (testing "should use custom truncation nouns"
      (let [truncate-noun "fruit"]
        (is (= "apple, orange, banana, pear, and 2 other fruits"
              (oxford items
                :truncate-noun truncate-noun)))
        (is (= "apple, orange, and banana"
              (oxford (take 3 items)
                :truncate-noun truncate-noun)))))

    (testing "should allow for different output conversion for the extra item count"
      (is (= "apple, orange, and four others"
            (oxford items
              :maximum-display 2
              :number-format numberword)))

      (is (= "apple and five other fruits"
            (oxford items
              :maximum-display 1
              :truncate-noun "fruit"
              :number-format numberword))))))

(def datetime-test-phrases
  (let [one-decade-in-years   10
        one-century-in-years  100
        one-millenia-in-years 1000]
    [["a moment ago" identity]
     ["a moment ago" #(jt.ldt/minus-nanos % 1000)]
     ["in a moment" #(jt.ldt/plus-nanos % 1000)]

     ["10 seconds ago" #(jt.ldt/minus-seconds % 10)]
     ["1 second ago" #(jt.ldt/minus-seconds % 1)]
     ["in 10 seconds" #(jt.ldt/plus-seconds % 10)]
     ["in 1 second" #(jt.ldt/plus-seconds % 1)]

     ["10 minutes ago" #(jt.ldt/minus-minutes % 10)]
     ["in 10 minutes" #(jt.ldt/plus-minutes % 10)]
     ["1 minute ago" #(jt.ldt/minus-minutes % 1)]
     ["in 1 minute" #(jt.ldt/plus-minutes % 1)]

     ["10 hours ago" #(jt.ldt/minus-hours % 10)]
     ["in 10 hours" #(jt.ldt/plus-hours % 10)]
     ["1 hour ago" #(jt.ldt/minus-hours % 1)]
     ["in 1 hour" #(jt.ldt/plus-hours % 1)]

     ["5 days ago" #(jt.ldt/minus-days % 5)]
     ["in 5 days" #(jt.ldt/plus-days % 5)]
     ["1 day ago" #(jt.ldt/minus-days % 1)]
     ["in 1 day" #(jt.ldt/plus-days % 1)]

     ["3 weeks ago" #(jt.ldt/minus-weeks % 3)]
     ["in 3 weeks" #(jt.ldt/plus-weeks % 3)]
     ["1 week ago" #(jt.ldt/minus-weeks % 1)]
     ["in 1 week" #(jt.ldt/plus-weeks % 1)]

     ["2 months ago" #(jt.ldt/minus-months % 2)]
     ["in 2 months" #(jt.ldt/plus-months % 2)]
     ["10 months ago" #(jt.ldt/minus-months % 10)]
     ["in 10 months" #(jt.ldt/plus-months % 10)]
     ["1 month ago" #(jt.ldt/minus-months % 1)]
     ["in 1 month" #(jt.ldt/plus-months % 1)]

     ["3 years ago" #(jt.ldt/minus-years % 3)]
     ["in 3 years" #(jt.ldt/plus-years % 3)]
     ["1 year ago" #(jt.ldt/minus-years % 1)]
     ["in 1 year" #(jt.ldt/plus-years % 1)]

     ["3 decades ago" #(jt.ldt/minus-years % (* 3 one-decade-in-years))]
     ["in 3 decades" #(jt.ldt/plus-years % (* 3 one-decade-in-years))]
     ["1 decade ago" #(jt.ldt/minus-years % one-decade-in-years)]
     ["in 1 decade" #(jt.ldt/plus-years % one-decade-in-years)]

     ["3 centuries ago" #(jt.ldt/minus-years % (* 3 one-century-in-years))]
     ["in 3 centuries" #(jt.ldt/plus-years % (* 3 one-century-in-years))]
     ["1 century ago" #(jt.ldt/minus-years % one-century-in-years)]
     ["in 1 century" #(jt.ldt/plus-years % one-century-in-years)]

     ["3 millennia ago" #(jt.ldt/minus-years % (* 3 one-millenia-in-years))]
     ["in 3 millennia" #(jt.ldt/plus-years % (* 3 one-millenia-in-years))]
     ["1 millenium ago" #(jt.ldt/minus-years % one-millenia-in-years)]
     ["in 1 millenium" #(jt.ldt/plus-years % one-millenia-in-years)]]))

(deftest datetime-test
  (let [t1-str "2022-01-01T01:00:00"
        t1     (jt.ldt/parse t1-str)]
    (is (= "a moment ago"
          (datetime (jt.ldt/now)))
      ":now-dt is optional")
    (testing "datetime accepts joda-time values"
      (is (= "a moment ago"
            (datetime (jt.ldt/now)
              :now-dt (jt.ldt/now))))
      (is (= "10 minutes ago"
            (datetime (jt.ldt/now)
              :now-dt (jt.ldt/plus-minutes (jt.ldt/now) 10)))))
    (testing "test phrases"
      (doseq [[phrase time-shift-fn] datetime-test-phrases]
        (is (= phrase
              (datetime (time-shift-fn t1)
                :now-dt t1)))))
    (testing "suffix and prefix"
      (is (= "10 minutes ago"
            (datetime (jt.ldt/minus-minutes t1 10)
              :prefix "foo"
              :now-dt t1))
        "prefix for a time in the past does nothing")
      (is (= "10 minutes in the glorious past"
            (datetime (jt.ldt/minus-minutes t1 10)
              :suffix "in the glorious past"
              :now-dt t1)))
      (is (= "forward, into our bright future 1 year"
            (datetime (jt.ldt/plus-years t1 1)
              :now-dt t1
              :prefix "forward, into our bright future")))
      (is (= "in 1 year"
            (datetime (jt.ldt/plus-years t1 1)
              :now-dt t1
              :suffix "foo"))
        "suffix for a time in the past does nothing"))))

(deftest durations
  (testing "duration to terms"
    (are [duration terms] (= terms (#'h/duration-terms duration))
                          ;; Less than a second is ignored
                          0 []
                          999 []
                          1000 [[1 "second"]]
                          ;; Remaining milliseconds after seconds are gnored
                          1500 [[1 "second"]]
                          ;; 0 periods are excluded
                          10805000 [[3 "hour"]
                                    [5 "second"]]))
  (testing "duration to string"
    (are [ms expected] (= expected (duration ms))
                       0 "less than a second"
                       999 "less than a second"
                       1000 "one second"
                       10805000 "three hours, five seconds")

    (are [ms options expected] (= expected (duration ms options))
                               999 {:short-text "just now"} "just now"
                               10805000 {:number-format str} "3 hours, 5 seconds"
                               510805000 {:number-format str
                                          :list-format   oxford} "5 days, 21 hours, 53 minutes, and 25 seconds")))
