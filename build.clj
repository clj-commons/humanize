(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'clojure-humanize/clojure-humanize)
(def version "0.3")

(defn make-version [opts]
  (or (:version opts)
      (str version "."
           (b/git-count-revs nil)
           (when (:snapshot opts)
             "-SNAPSHOT"))))

(def base-opts
  {:lib       lib
   :class-dir "target/classes"})

(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "cljs-test-runner-out"}))

(defn jar
  [opts]
  (let [version (make-version opts)
        opts' (-> base-opts
                  (merge opts)
                  (assoc :version version))]
    (bb/jar opts')))

(defn deploy
  "Build and deploy the JAR to Clojars."
  [opts]
  (-> opts
      jar
      bb/deploy))
