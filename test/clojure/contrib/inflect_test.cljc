(ns clojure.contrib.inflect-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is are]])
            [clojure.contrib.inflect :refer [pluralize-noun]]))

(deftest pluralize-noun-test

  (testing "A count of one returns the standard value"
    (are [noun] (= noun (pluralize-noun 1 noun))
                "kiss"
                "robot"
                "ox"))

  (testing "Zero is considered plural"
    (are [noun expected-noun] (= expected-noun (pluralize-noun 0 noun))
                              "kiss" "kisses"
                              "robot" "robots"
                              "ox" "oxen"))

  (testing "Testing nouns ending in a sibilant sound."
    (is (= (pluralize-noun 2 "kiss") "kisses"))
    (is (= (pluralize-noun 2 "phase") "phases"))
    (is (= (pluralize-noun 2 "dish") "dishes"))
    (is (= (pluralize-noun 2 "witch") "witches")))

  (testing "Testing Nouns ending in y."
    (is (= (pluralize-noun 2 "boy") "boys"))
    (is (= (pluralize-noun 2 "holiday") "holidays"))
    (is (= (pluralize-noun 2 "party") "parties"))
    (is (= (pluralize-noun 2 "nanny") "nannies")))

  (testing "Testing nounse ending in F o FE"
    (is (= (pluralize-noun 2 "life") "lives"))
    (is (= (pluralize-noun 2 "thief") "thieves"))
    (is (= (pluralize-noun 2 "chief") "chiefs"))
    (is (= (pluralize-noun 2 "roof") "roofs"))
    (is (= (pluralize-noun 2 "staff") "staffs")))

  (testing "Testing general nouns."
    (is (= (pluralize-noun 2 "car") "cars"))
    (is (= (pluralize-noun 2 "house") "houses"))
    (is (= (pluralize-noun 2 "book") "books"))
    (is (= (pluralize-noun 2 "bird") "birds"))
    (is (= (pluralize-noun 2 "pencil") "pencils")))

  (testing "Testing irregulars nouns"
    (are [noun expected-noun] (= expected-noun (pluralize-noun 2 noun))
                              "ox" "oxen"
                              "moose" "moose"
                              "hero" "heroes"
                              "cactus" "cactuses")))
