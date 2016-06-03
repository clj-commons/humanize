(ns clojure.contrib.inflect-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is]])
            [clojure.contrib.inflect :refer [pluralize-noun]]))

(deftest pluralize-noun-test
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
    (is (= (pluralize-noun 2 "roof") "roofs")))

  (testing "Testing general nouns."
    (is (= (pluralize-noun 2 "car") "cars"))
    (is (= (pluralize-noun 2 "house") "houses"))
    (is (= (pluralize-noun 2 "book") "books"))
    (is (= (pluralize-noun 2 "bird") "birds"))
    (is (= (pluralize-noun 2 "pencil") "pencils")))

  (testing "Testing irregulars nouns"
    (is (= (pluralize-noun 2 "ox") "oxen"))
    (is (= (pluralize-noun 2 "moose") "moose"))
    (is (= (pluralize-noun 2 "hero") "heroes")))
  )
