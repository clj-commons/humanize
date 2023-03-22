(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'clj-commons/humanize)
(def version "0.3")

(defn make-version [opts]
  (or (:version opts)
      (str version "."
           (b/git-count-revs nil)
           (when-not (:release opts)
             "-SNAPSHOT"))))

(def base-opts
  {:lib       lib
   :class-dir "target/classes"})

(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "cljs-test-runner-out"}))

(defn jar
  "Build a JAR."
  [opts]
  (let [version (make-version opts)
        opts' (-> base-opts
                  (merge opts)
                  (assoc :version version))]
    (bb/jar opts')))

(defn deploy
  "Build and deploy the JAR to Clojars.  Defaults to a snapshot,
  specify :release true for a final release."
  [opts]
  (-> opts
      jar
      bb/deploy))
