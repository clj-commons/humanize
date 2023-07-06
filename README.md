
# org.clj-commons/humanize

[![Clojars Project](https://img.shields.io/clojars/v/org.clj-commons/humanize.svg)](https://clojars.org/org.clj-commons/humanize)
![clojure.yml](https://github.com/clj-commons/humanize/actions/workflows/clojure.yml/badge.svg?event=push)
[![cljdoc badge](https://cljdoc.org/badge/org.clj-commons/humanize)](https://cljdoc.org/d/org.clj-commons/humanize)

A Clojure(script) library to produce human-readable strings for numbers, dates, and more
based on similar libraries in other languages

## Usage

* [numberword](#numberword)
* [intcomma](#intcomma)
* [intword](#intword)
* [ordinal](#ordinal)
* [filesize](#filesize)
* [truncate](#truncate)
* [oxford](#oxford)
* [pluralize-noun](#pluralize-noun)
* [datetime](#datetime)
* [duration](#duration)

### numberword

Takes a number and return a full written string form. For example,
23237897 will be written as "twenty-three million two hundred and
thirty-seven thousand eight hundred and ninety-seven".

```clojure
user> (require '[clj-commons.humanize :as h])
nil

user> (h/numberword 3567)
"three thousand five hundred and sixty-seven"

user> (h/numberword 25223)
"twenty-five thousand two hundred and twenty-three"

user> (h/numberword 23237897)
"twenty-three million two hundred and thirty-seven thousand eight hundred and ninety-seven"
```

### intcomma

Converts an integer to a string containing commas every three digits.

```clojure
user>  (h/intcomma 1000)
"1,000"

user>  (h/intcomma 10123)
"10,123"

user>  (h/intcomma 10311)
"10,311"

user>  (h/intcomma 1000000)
"1,000,000"
```

### intword

Converts a large integer to a friendly text representation. Works best
for numbers over 1 million. For example, 1000000 becomes '1.0
million', 1200000 becomes '1.2 million' and '1200000000' becomes '1.2
billion'.  Supports up to decillion (33 digits) and googol (100
digits).  

```clojure
user>  (h/intword 2000000000)
"2.0 billion"

user>  (h/intword 6000000000000)
"6.0 trillion"

user>  (h/intword 3500000000000000000000N)
"3.5 sextillion"

user>  (h/intword 8100000000000000000000000000000000N)
"8.1 decillion"
```

### ordinal

Converts an integer to its ordinal as a string.

```clojure
user> (h/ordinal 1)
"1st"

user>  (h/ordinal 2)
"2nd"

user>  (h/ordinal 4)
"4th"

user>  (h/ordinal 11)
"11th"

user>  (h/ordinal 111)
"111th"
```

### filesize

Format a number of bytes as a human-readable filesize (eg. 10 kB).
By default, decimal suffixes (kB, MB) are used.  Passing the :binary option as true
will use binary suffixes (KiB, MiB) are used.

The :format option gives more control over how the numeric part of the output filesize
is created.

```clojure
user>  (h/filesize 3000000 :binary false)
"3.0MB"

user>  (h/filesize 3000000000000 :binary false)
"3.0TB"

user>  (h/filesize 3000 :binary true :format "%.2f")
"2.93KiB"

user>  (h/filesize 3000000 :binary true)
"2.9MiB"
```

### truncate

Truncate a string with suffix (ellipsis by default) if it is longer
than specified length.

```clojure
user> (h/truncate "abcdefghijklmnopqrstuvwxyz" 10)
"abcdefg..."

user> (h/truncate "abcdefghijklmnopqrstuvwxyz" 10 "[more]")
"abcd[more]"
```

### oxford

Converts a list of items to a human-readable string with an optional
limit.

```clojure
user> (h/oxford ["apple" "orange" "mango"])
"apple, orange, and mango"

user> (h/oxford ["apple" "orange" "mango" "pear"]
                                       :maximum-display 2)
"apple, orange, and 2 others"

user> (h/oxford ["apple" "orange" "mango" "pear"]
                                       :maximum-display 2
                                       :truncate-noun "fruit")
"apple, orange, and 2 other fruits"

user> (h/oxford ["apple" "orange" "mango" "pear"]
        :maximum-display 2
        :number-format h/numberword
        :truncate-noun "fruit")
"apple, orange, and two other fruits"
```

### pluralize-noun

Return the pluralized noun if the given number is not 1.

```clojure
user (require '[clj-commons.humanize.inflect :as i])
nil

user> (i/pluralize-noun 2 "thief")
"thieves"

user> (i/pluralize-noun 3 "tomato")
"tomatoes"

user> (i/pluralize-noun 4 "roof")
"roofs"

user> (i/pluralize-noun 5 "person")
"people"

user> (i/pluralize-noun 6 "buzz")
"buzzes"
```

Other functions in the inflect namespace are used to extend the rules
for how particular words, or particular letter patterns in words, 
can be pluralized.

### datetime

Given a datetime or date, return a human-friendly representation
of the amount of time difference, relative to the current time.

```clojure
user> (require '[clj-time.core :as t])
nil

user> (h/datetime (t/plus (t/now) (t/seconds -30)))
"30 seconds ago"

user> (h/datetime (t/plus (t/now) (t/seconds 30)))
"in 30 seconds"

user> (h/datetime (t/plus (t/now) (t/years -20)))
"2 decades ago"

user> (h/datetime (t/plus (t/now) (t/years -7)))
"7 years ago"

```

### duration

Given a duration in milliseconds, return a human-friendly
representation of the amount of time passed.

```clojure
user> (h/duration 2000)
"two seconds"

user> (h/duration 325100)
"five minutes, twenty-five seconds"

user> (h/duration 500)
"less than a second"

user> (h/duration 325100 {:number-format str})
=> "5 minutes, 25 seconds"

```

## Linting

Run:

```sh
 clj -M:clj-kondo --lint src
```

## Running Tests

JVM tests can be run with just:

```
clojure -X:test
```

For cljs, you will need node/npm in order to install karma:

```sh
npm install -g karma karma-cljs-test karma-chrome-launcher karma-firefox-launcher
```

Then tests can be run with:

```clj
clojure -M:cljs-test -x chrome-headless
```

Or `-x firefox-headless`.

## Deployment

Check [deps-deploy README](https://github.com/slipset/deps-deploy) for details regarding clojars credentials.

Build a snapshot jar:

```clj
clojure -T:build jar
```

Deploy a snapshot:

```clj
 clojure -T:build deploy 
```

Set `:release` to `true` for a release version (make sure the version number in `build.clj` is correct first):

```clj
clojure -T:build deploy :release true
```

## TODO

+ Add other missing functions
* [JS-humanize](https://github.com/milanvrekic/JS-humanize)
* [coffee-humanize](https://github.com/HubSpot/humanize/)


## License

Copyright 2015-2023 Thura Hlaing

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
