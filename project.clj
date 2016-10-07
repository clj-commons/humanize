(defproject clojure-humanize "0.2.1"
  :description "produce human readable strings in clojure"
  :url "https://github.com/trhura/clojure-humanize"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [clj-time "0.12.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "1.9.93"]]
                   :plugins [[lein-cljsbuild "1.1.3"]]}}
  :cljsbuild {:builds {:test
                       {:source-paths ["src" "test"]
                        :compiler {:optimizations :whitespace
                                   :pretty-print true
                                   :output-dir "target/js/test"
                                   :output-to "target/humanize-test.js"
                                   :source-map "target/humanize-test.js.map"}}}
              :test-commands {"cljs" ["phantomjs"
                                      "phantom/unit-test.js"
                                      "phantom/unit-test.html"]}}
  :aliases {"test-all" ["do"
                        "test" "clojure.contrib.cljc-test,"
                        "cljsbuild" "test"
                        ]})
