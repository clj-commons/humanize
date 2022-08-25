(ns build
  (:require [clojure.tools.build.api :as b]
            [org.corfield.build :as bb]))

(def lib 'clojure-humanize/clojure-humanize)
(defn- the-version [patch] (format "1.0.%s" patch))
(def version (the-version (b/git-count-revs nil)))
(def snapshot (the-version "999-SNAPSHOT"))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))

(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "cljs-test-runner-out"}))

(defn jar [opts]
  (b/write-pom {:class-dir class-dir
                :lib       lib
                :version   (if (:snapshot opts) snapshot version)
                :basis     basis
                :src-dirs  ["src"]})
  (b/copy-dir {:src-dirs   ["src" "resources"]
               :target-dir class-dir})
  (let [jar-file (format "target/%s-%s.jar" (name lib) (if (:snapshot opts) snapshot version))]
    (b/jar {:class-dir class-dir
            :jar-file  jar-file})))

(defn deploy "Deploy the JAR to Clojars." [opts]
  (println "**" opts)
  (-> opts
    (assoc :lib lib :version (if (:snapshot opts) snapshot version))
    (bb/deploy)))
