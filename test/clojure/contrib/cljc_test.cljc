(ns clojure.contrib.cljc-test
  (:require #?(:clj [clojure.test :as test]
               :cljs [cljs.test :as test :include-macros true])
                    [clojure.contrib.humanize-test]
                    [clojure.contrib.inflect-test]))

#?(:cljs (enable-console-print!))

#?(:cljs
   (defmethod test/report [::test/default :summary] [m]
              (println "\nRan" (:test m) "tests containing"
                       (+ (:pass m) (:fail m) (:error m)) "assertions.")
              (println (:fail m) "failures," (:error m) "errors.")
              (aset js/window "test-failures" (+ (:fail m) (:error m)))))

(defn test-runner []
  (test/run-tests
    'clojure.contrib.humanize-test
    'clojure.contrib.inflect-test))
