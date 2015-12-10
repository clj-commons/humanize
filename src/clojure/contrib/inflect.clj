(ns clojure.contrib.inflect)
  ;;(:require [clojure.contrib.inflect :refer :all]))

(defn in? [x coll]
  "Return true if x is in coll, else false. "
  ;; FIXME: duplicate
  (some #(= x %) coll))

(def ^:dynamic *pluralize-noun-rules* (atom []))
(def ^:dynamic *pluralize-noun-exceptions* (atom {}))

(defn pluralize-noun [count noun]
  "Return the pluralized noun if the `count' is
   greater than 1."
  (let [singular? (<= count 1)]
    (if singular? noun ;; If singular, return noun
        (some (fn [[cond? result-fn]]
                (if (cond? noun)
                  (result-fn noun)))
              @*pluralize-noun-rules*))))

(defn add-pluralize-noun-rule
  [rule-description cond? result-fn]
  (swap! *pluralize-noun-rules*
          conj
          [cond? result-fn]))

(defn add-pluralize-noun-exceptions
  [execption-description exceptions]
  (swap! *pluralize-noun-exceptions* into exceptions))

;; the order of rules is important
(add-pluralize-noun-rule "For irregular nouns, use the exceptions."
                         (fn [noun] (contains? @*pluralize-noun-exceptions* noun))
                         (fn [noun] (@*pluralize-noun-exceptions* noun)))

(add-pluralize-noun-rule "For nouns ending within consonant + y, suffixes `ies' "
                         (fn [noun] (and (.endsWith noun "y")
                                         (not (boolean (in?  (-> noun butlast last) ;; before-last char
                                                             [\a \e \i \o \u])))))
                         (fn [noun] (str (-> noun butlast clojure.string/join) "ies")))

(add-pluralize-noun-rule "For nouns ending with ss, x, z, ch or sh, suffixes `es.'"
                         (fn [noun] (some #(.endsWith noun %)
                                          ["ss" "x" "z" "ch" "sh"]))
                         (fn [noun] (str noun "es")))

(add-pluralize-noun-rule "For nouns ending with `f', suffixes `ves'"
                         (fn [noun] (.endsWith noun "f"))
                         (fn [noun] (str (-> noun butlast clojure.string/join) "ves")))

(add-pluralize-noun-rule "For nouns ending with `fe', suffixes `ves'"
                         (fn [noun] (.endsWith noun "fe"))
                         (fn [noun] (str (-> noun butlast butlast clojure.string/join) "ves")))

(add-pluralize-noun-rule "Always append `s' at the end of noun."
                         (fn [noun] true) ;; always return true
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
