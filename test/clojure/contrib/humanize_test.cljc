(ns clojure.contrib.humanize-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is are]])
            [clojure.contrib.humanize :refer [intcomma ordinal intword numberword
                                              filesize truncate oxford datetime
                                              duration]
             :as h]
            [clojure.contrib.inflect :refer [pluralize-noun]]
            #?(:clj [clojure.math.numeric-tower :refer [expt]])
            #?(:clj  [clj-time.core  :refer [now from-now seconds millis minutes
                                             hours days weeks months years plus]]
               :cljs [cljs-time.core :refer [now from-now seconds millis minutes
                                             hours days weeks months years plus]])
            #?(:clj  [clj-time.local  :refer [local-now]]
               :cljs [cljs-time.local :refer [local-now]])
            #?(:clj  [clj-time.coerce  :refer [to-date-time to-string]]
               :cljs [cljs-time.coerce :refer [to-date-time to-string]])))

#?(:cljs (def ^:private expt (.-pow js/Math)))

(deftest intcomma-test
  (testing "Testing intcomma function with expected data."
    (doseq [[testnum result] [[100, "100"], [1000, "1,000"],
                              [10123, "10,123"], [10311, "10,311"],
                              [1000000, "1,000,000"], [-100, "-100"],
                              [-10123 "-10,123"], [-10311 "-10,311"],
                              [-1000000, "-1,000,000"]]]
      (is (= (intcomma testnum) result)))))

(deftest ordinal-test
  (testing "Testing ordinal function with expected data."
    (doseq [[testnum result] [[1,"1st"], [ 2,"2nd"],
                              [ 3,"3rd"], [ 4,"4th"],
                              [ 11,"11th"],[ 12,"12th"],
                              [ 13,"13th"], [ 101,"101st"],
                              [ 102,"102nd"], [ 103,"103rd"],
                              [111, "111th"]]]
      (is (= (ordinal testnum) result)))))

(deftest intword-test
  (testing "Testing intword function with expected data."
    (doseq [[testnum result format] [[100 "100.0"]
                                     [ 1000000 "1.0 million"]
                                     [ 1200000 "1.2 million"]
                                     [ 1290000 "1.3 million"]
                                     [ 1000000000 "1.0 billion"]
                                     [ 2000000000  "2.0 billion"]
                                     [ 6000000000000 "6.0 trillion"]
                                     [1300000000000000 "1.3 quadrillion"]
                                     [3500000000000000000000 "3.5 sextillion"]
                                     [8100000000000000000000000000000000 "8.1 decillion"]
                                     [1230000 "1.23 million" "%.2f"]
                                     [(expt 10 101) "10.0 googol"]
                                     ]]
      ;; default argument
      (let [format (if (nil? format) "%.1f" format)]
        (is (= (intword testnum
                        :format format
                        )
               result))))))

(deftest numberword-test
  (testing "Testing numberword function with expected data."
    (doseq [[testnum result] [[0 "zero"]
                              [7 "seven"]
                              [12 "twelve"]
                              [40 "forty"]
                              [94 "ninety-four"]
                              [100 "one hundred"]
                              [51 "fifty-one"]
                              [234 "two hundred and thirty-four"]
                              [1000 "one thousand"]
                              [3567 "three thousand five hundred and sixty-seven"]
                              [44120 "forty-four thousand one hundred and twenty"]
                              [25223 "twenty-five thousand two hundred and twenty-three"]
                              [5223 "five thousand two hundred and twenty-three"]
                              [1000000 "one million"]
                              [23237897 "twenty-three million two hundred and thirty-seven thousand eight hundred and ninety-seven"]]]
      ;; default argument
      (is (= (numberword testnum) result)))))

(deftest filesize-test
  (testing "Testing filesize function with expected data."
    (doseq [[testsize result binary format] [[0, "0"]
                                             [300, "300.0B"]
                                             [3000, "3.0KB"]
                                             [3000000, "3.0MB"]
                                             [3000000000, "3.0GB"]
                                             [3000000000000, "3.0TB"]
                                             [3000, "2.9KiB", true]
                                             [3000000, "2.9MiB", true]
                                             [(* (expt 10 26) 30), "3000.0YB"]
                                             [(* (expt 10 26) 30), "2481.5YiB", true]

                                             ]]
      ;; default argument
      (let [binary (boolean binary)
            format (if (nil? format) "%.1f" format)]
        (is (= (filesize testsize
                         :binary binary
                         :format format
                         )
               result))))))

(deftest truncate-test
  (testing "truncate should not return a string larger than give length."
    (let [string "asdfghjkl" ]
      (is (= (count (truncate string 7)) 7))
      (is (= (count (truncate string 7 "1234")) 7))
      (is (= (count (truncate string 100)) (count string)))))

  (testing "testing truncate with expected data."
    (let [string "abcdefghijklmnopqrstuvwxyz"]
      (is (= (truncate string 14) "abcdefghijk..."))
      (is (= (truncate string 14 "...kidding") "abcd...kidding")))))

(deftest oxford-test
  (let [items ["apple", "orange", "banana", "pear", "pineapple", "strawberry"]]
    (testing "should return an empty string when given an empty list."
      (is (= (oxford []) "")))

    (testing "should return a string version of a list that has only one value."
      (is (= (oxford [(items 0)]) (items 0))))

    (testing "should return a string with no commas & items separated with `and` when passed exactly two values in list"
      (is (oxford (take 2 items)) (str (items 0) " and " (items 1))))

    (testing "should return items separated by `and' when given a list of values"
      (is (= (oxford (take 2 items)) (str (items 0) " and " (items 1))))
      (is (= (oxford (take 3 items)) (str (items 0) ", "
                                          (items 1) ", and " (items 2))))
      (is (= (oxford (take 4 items)) (str (items 0) ", "
                                          (items 1) ", "
                                          (items 2) ", and " (items 3)))))

    (testing "should truncate a large list of items with proper pluralization"
      (is (= (oxford (take 5 items)) (str (items 0) ", "
                                          (items 1) ", "
                                          (items 2) ", "
                                          (items 3) ", and " 1 " other")))
      (is (= (oxford (take 5 items)
                     :maximum-display 2)
             (str (items 0) ", "
                  (items 1) ", and " 3 " others")))
      (is (= (oxford (take 2 items) 
                     :maximum-display 1)
             (str (items 0) " and " "1 other"))))

    (testing "should accept custom trucation strings"
      (let [truncate-noun "fruit"]
        (is (oxford (take 5 items)
                    :truncate-noun truncate-noun)
            (str (items 0) ", "
                 (items 1) ", "
                 (items 2) ", and " 2 " other " (pluralize-noun 2 truncate-noun)))
        (is (oxford (take 3 items)
                    :truncate-string truncate-noun)
            (str (items 0) ", "
                 (items 1) ", and " (items 2)))))))

(deftest datetime-test
  (let [past (fn [n unit] (datetime (now) :now-dt (-> n unit from-now)))
        future (fn [n unit] (datetime (plus (-> n unit from-now) (millis 300)) ; fix delayed execution by adding some millis
                                      :now-dt (now)))]
    (testing "date diff to text"
      (are [expected diff] (= expected diff)
                           "a moment ago" (datetime (now))
                           "in a moment" (datetime (-> 500 millis from-now))
                           "10 seconds ago" (past 10 seconds)
                           "in 10 seconds" (future 10 seconds)
                           "1 second ago" (past 1 seconds)
                           "in 1 second" (future 1 seconds)
                           "10 minutes ago" (past 10 minutes)
                           "in 10 minutes" (future 10 minutes)
                           "1 minute ago" (past 1 minutes)
                           "in 1 minute" (future 1 minutes)
                           "10 hours ago" (past 10 hours)
                           "in 10 hours" (future 10 hours)
                           "1 hour ago" (past 1 hours)
                           "in 1 hour" (future 1 hours)
                           "5 days ago" (past 5 days)
                           "in 5 days" (future 5 days)
                           "1 day ago" (past 1 days)
                           "in 1 day" (future 1 days)
                           "1 week ago" (past 1 weeks)
                           "in 1 week" (future 1 weeks)
                           "3 weeks ago" (past 3 weeks)
                           "in 3 weeks" (future 3 weeks)
                           "2 months ago" (past 10 weeks)
                           "in 2 months" (future 10 weeks)
                           "10 months ago" (past 10 months)
                           "in 10 months" (future 10 months)
                           "1 month ago" (past 1 months)
                           "in 1 month" (future 1 months)
                           "3 years ago" (past 3 years)
                           "in 3 years" (future 3 years)
                           "1 year ago" (past 1 years)
                           "in 1 year" (future 1 years)
                           "3 decades ago" (past 30 years)
                           "in 3 decades" (future 30 years)
                           "1 decade ago" (past 10 years)
                           "in 1 decade" (future 10 years)
                           "3 centuries ago" (past (* 3 100) years)
                           "in 3 centuries" (future (* 3 100) years)
                           "3 millennia ago" (past (* 3 1000) years)
                           "1 millenium ago" (past 1000 years)
                           "in 3 millennia" (future (* 3 1000) years)
                           "in 1 millenium" (future 1000 years)))))

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
                                          :list-format oxford} "5 days, 21 hours, 53 minutes, and 25 seconds")))
