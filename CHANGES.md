## 0.2.1 - UNRELEASED

`clojure.contrib.inflect/pluralize-noun` now pluralizes a count of zero; previously any count less
than or equal to 1 was considered singular.

Added `clojure.contrib.humanize/duration` and `duration-terms` to format a duration, in
milliseconds, as a string.
