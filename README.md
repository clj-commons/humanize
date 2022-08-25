
# clojure-humanize

![clojure.yml](https://github.com/trhura/clojure-humanize/actions/workflows/clojure.yml/badge.svg)

A Clojure(script) library to produce human readable strings for numbers, dates
based on similar libraries in other languages

## Installation

__([via Clojars](https://clojars.org/clojure-humanize))__

[![Clojars Project](http://clojars.org/clojure-humanize/latest-version.svg)](http://clojars.org/clojure-humanize)

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
user> (numberword 3567)
"three thousand five hundred and sixty-seven"

user> (numberword 25223)
"twenty-five thousand two hundred and twenty-three"

user> (numberword 23237897)
"twenty-three million two hundred and thirty-seven thousand eight hundred and ninety-seven"
```

### intcomma

Converts an integer to a string containing commas. every three digits.

```clojure
user>  (clojure.contrib.humanize/intcomma 1000)
1,000

user>  (clojure.contrib.humanize/intcomma 10123)
10,123

user>  (clojure.contrib.humanize/intcomma 10311)
10,311

user>  (clojure.contrib.humanize/intcomma 1000000)
1,000,000
```

### intword

Converts a large integer to a friendly text representation. Works best
for numbers over 1 million. For example, 1000000 becomes '1.0
million', 1200000 becomes '1.2 million' and '1200000000' becomes '1.2
billion'.  Supports up to decillion (33 digits) and googol (100
digits).

```clojure
user>  (clojure.contrib.humanize/intword 2000000000)
2.0 billion

user>  (clojure.contrib.humanize/intword 6000000000000)
6.0 trillion

user>  (clojure.contrib.humanize/intword 3500000000000000000000N)
3.5 sextillion

user>  (clojure.contrib.humanize/intword 8100000000000000000000000000000000N)
8.1 decillion
```

### ordinal

Converts an integer to its ordinal as a string.

```clojure
user>  (clojure.contrib.humanize/ordinal 2)
2nd

user>  (clojure.contrib.humanize/ordinal 4)
4th

user>  (clojure.contrib.humanize/ordinal 11)
11th

user>  (clojure.contrib.humanize/ordinal 111)
111th
```

### filesize

Format a number of byteslike a human readable filesize (eg. 10 kB).
By default, decimal suffixes (kB, MB) are used.  Passing binary=true
will use binary suffixes (KiB, MiB) are used.

```clojure
user>  (clojure.contrib.humanize/filesize 3000000 :binary false)
3.0MB

user>  (clojure.contrib.humanize/filesize 3000000000000 :binary false)
3.0TB

user>  (clojure.contrib.humanize/filesize 3000 :binary true :format " %.2f "" ")
2.93KiB

user>  (clojure.contrib.humanize/filesize 3000000 :binary true)
2.9MiB
```

### truncate

Truncate a string with suffix (ellipsis by default) if it is longer
than specified length.

```clojure
user> (clojure.contrib.humanize/truncate "abcdefghijklmnopqrstuvwxyz" 10)
"abcdefg..."

user> (clojure.contrib.humanize/truncate "abcdefghijklmnopqrstuvwxyz" 10 "...xyz")
"abcd...xyz"
```

### oxford
Converts a list of items to a human readable string with an optional
limit.

```clojure
user> (clojure.contrib.humanize/oxford ["apple" "orange" "mango"])
"apple, orange, and mango"

user> (clojure.contrib.humanize/oxford ["apple" "orange" "mango" "pear"]
                                       :maximum-display 2)
"apple, orange, and 2 others"

user> (clojure.contrib.humanize/oxford ["apple" "orange" "mango" "pear"]
                                       :maximum-display 2
                                       :truncate-noun "fruit")
"apple, orange, and 2 other fruits"
```

### pluralize-noun

Return the pluralized noun if the given number is not 1.

```clojure
user> (clojure.contrib.inflect/pluralize-noun 2 "thief")
"thieves"

user> (clojure.contrib.inflect/pluralize-noun 3 "tomato")
"tomatoes"

user> (clojure.contrib.inflect/pluralize-noun 4 "roof")
"roofs"

user> (clojure.contrib.inflect/pluralize-noun 5 "person")
"people"

user> (clojure.contrib.inflect/pluralize-noun 6 "buzz")
"buzzes"

```

### datetime

Given a datetime or date, return a human-friendly representation
of the amount of time difference, relative to the current time.

```clojure
user> (clojure.contrib.humanize/datetime (plus (now) (seconds -30)))
"30 seconds ago"

user> (clojure.contrib.humanize/datetime (plus (now) (seconds 30)))
"in 30 seconds"

user> (clojure.contrib.humanize/datetime (plus (now) (years -20)))
"2 decades ago"

user> (clojure.contrib.humanize/datetime (plus (now) (years -7)))
"7 years ago"

```

### duration

Given a duration in milliseconds, return a human-friendly
representation of the amount of time passed.

```clojure
user> (clojure.contrib.humanize/duration 2000)
"two seconds"

user> (clojure.contrib.humanize/duration 325100)
"five minutes, twenty-five seconds"

user> (clojure.contrib.humanize/duration 500)
"less than a second"

user> (clojure.contrib.humanize/duration 325100 {:number-format str})
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
clojure -Mtest
```

For cljs, you will need node/npm in order to install karma:

```sh
npm install -g karma karma-cljs-test karma-chrome-launcher karma-firefox-launcher
```

Then tests can be run with:

```clj
clojure -Mcljs-test -x chrome-headless
```

Or `-x firefox-headless`.

## Deployment

Check [deps-deploy README](https://github.com/slipset/deps-deploy) for details regarding clojars credentials.

Build a jar:

```clj
clojure -T:build jar
```

Deploy:

```clj
 clojure -T:build deploy 
```

Set `:snapshot` to `true` for a snapshot version:

```clj
clojure -T:build jar :snapshot true 
clojure -T:build deploy :snapshot true
```

## TODO

+ Add other missing functions
* [JS-humanize](https://github.com/milanvrekic/JS-humanize)
* [coffee-humanize](https://github.com/HubSpot/humanize/)


## License

Copyright © 2015 Thura Hlaing

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
