# clojure-humanize

A Clojure library to produce human readable strings for numbers, dates
based on similar python, javascript libraries

## Usage

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

## TODO

+ Add other missing functions
* [JS-humanize](https://github.com/milanvrekic/JS-humanize)
* [coffee-humanize](https://github.com/HubSpot/humanize/)


## License

Copyright © 2014 Thura Hlaing

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
