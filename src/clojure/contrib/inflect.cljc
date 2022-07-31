(ns clojure.contrib.inflect
  "Functions and rules for pluralizing nouns."
  (:require [clojure.string :refer [ends-with?]]))

(defn in?
  "Return true if x is in coll, else false."
  [x coll]
  ;; FIXME: duplicate
  (some #(= x %) coll))

(def ^:private pluralize-noun-rules (atom []))
(def ^:private pluralize-noun-exceptions (atom {}))

(defn pluralize-noun
  "Return the pluralized noun if the `count' is not 1."
  [count noun]
  {:pre [(<= 0 count)]}
  (let [singular? (== count 1)]
    (if singular?
      noun                                                  ; If singular, return noun
      (some (fn [[cond? result-fn]]
                (when (cond? noun)
                  (result-fn noun)))
            @pluralize-noun-rules))))

(defn add-pluralize-noun-rule
  "Adds a rule for pluralizing. The singular form of the noun is passed to the cond?
  predicate and if that return a truthy value, the singular form is passed
  to the result-fn to generate the plural form.

  The rule description is for documentation only, it is ignored and may be nil."
  [_rule-description cond? result-fn]
  (swap! pluralize-noun-rules
         conj
         [cond? result-fn]))

(defn add-pluralize-noun-exceptions
  "Adds some number of exception cases.

   exceptions is a map from singular form to plural form.

   The exception description is for documentation only, it is ignored and may be nil."
  [_exception-description exceptions]
  (swap! pluralize-noun-exceptions into exceptions))

;; the order of rules is important
(add-pluralize-noun-rule "For irregular nouns, use the exceptions."
                         (fn [noun] (contains? @pluralize-noun-exceptions noun))
                         (fn [noun] (@pluralize-noun-exceptions noun)))

(add-pluralize-noun-rule "For nouns ending within consonant + y, suffixes `ies' "
                         (fn [noun] (and (ends-with? noun "y")
                                         (not (boolean (in?  (-> noun butlast last) ;; before-last char
                                                             [\a \e \i \o \u])))))
                         (fn [noun] (str (-> noun butlast clojure.string/join) "ies")))

(add-pluralize-noun-rule "For nouns ending with ss, x, z, ch or sh, suffixes `es.'"
                         (fn [noun] (some #(ends-with? noun %)
                                          ["ss" "x" "z" "ch" "sh"]))
                         (fn [noun] (str noun "es")))

(add-pluralize-noun-rule "For nouns ending with `f', suffixes `ves'"
                         (fn [noun] (and (ends-with? noun "f")
                                         (not (ends-with? noun "ff"))))
                         (fn [noun] (str (-> noun butlast clojure.string/join) "ves")))

(add-pluralize-noun-rule "For nouns ending with `fe', suffixes `ves'"
                         (fn [noun] (ends-with? noun "fe"))
                         (fn [noun] (str (-> noun butlast butlast clojure.string/join) "ves")))

(add-pluralize-noun-rule "Always append `s' at the end of noun."
                         (fn [_noun] true) ;; always return true
                         (fn [noun] (str noun "s")))

(add-pluralize-noun-exceptions "Irregular nouns ending in en"
                               {
                                "ox" "oxen",
                                "child" "children",
                                "man" "men",
                                "woman" "women",
                                "foot" "feet",
                                "tooth" "teeth",
                                "goose" "geese",
                                "mouse" "mice" ,
                                "person" "people",
                                "louse" "lice",
                                })


(add-pluralize-noun-exceptions "Irregular nouns ending in f"
                               {
                                "chef" "chefs",
                                "cliff" "cliffs",
                                "ref" "refs",
                                "roof" "roofs",
                                "chief" "chiefs",
                                }
                               )

(add-pluralize-noun-exceptions "Irregular nouns ending in o-es"
                               {
                                "negro" "negroes",
                                "buffalo" "buffaloes",
                                "flamingo" "flamingoes",
                                "hero" "heroes",
                                "mango" "mangoes",
                                "mosquito" "mosquitoes",
                                "potato" "potatoes",
                                "tomato" "tomatoes",
                                "tornado" "tornadoes",
                                "torpedo" "torpedoes",
                                "tuxedo" "tuxedoes",
                                "volcano" "volcanoes",
                                "zero" "zeroes",
                                "echo" "echoes",
                                "banjo" "banjoes",
                                "cactus" "cactuses"
                                }
                               )

(add-pluralize-noun-exceptions "Nouns with identical singular and plural forms."
                               {
                                "bison" "bison",
                                "buffalo" "buffalo",
                                "deer" "deer",
                                "duck" "duck",
                                "fish" "fish",
                                "moose" "moose",
                                "pike" "pike",
                                "sheep" "sheep",
                                "salmon" "salmon",
                                "trout" "trout",
                                "swine" "swine",
                                "plankton" "plankton",
                                "squid" "squid",
                                })

(add-pluralize-noun-exceptions "Special cases"
                               {
                                "millenium" "millennia",
                                })
