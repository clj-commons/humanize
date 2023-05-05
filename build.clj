(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

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
   :basis     (b/create-basis {:project "deps.edn"})
   :src       ["src"]
   :class-dir "target/classes"})

(defn clean [_]
  (b/delete {:path "target"})
  (b/delete {:path "cljs-test-runner-out"}))

(defn jar
  "Build a JAR."
  [opts]
  (let [version (make-version opts)
        {:keys [src class-dir] :as opts'} (-> base-opts
                                              (merge opts)
                                              (assoc :jar-file (format "target/%s-%s.jar" (name lib) version)
                                                     :version  version))]
    (b/write-pom opts')
    (b/copy-dir {:src-dirs   src
                 :target-dir class-dir})
    (b/jar opts')
    opts'))

(defn deploy-clojars
  "Deploy to Clojars."
  [{:keys [jar-file class-dir] :as opts}]
  (let [opts' (merge opts
                     {:installer :remote
                      :artifact (b/resolve-path jar-file)
                      :pom-file (b/pom-path {:lib lib
                                             :class-dir class-dir})})]
    (dd/deploy opts')
    opts'))

(defn deploy
  "Build and deploy the JAR to Clojars.  Defaults to a snapshot,
  specify :release true for a final release."
  [opts]
  (-> opts
      jar
      deploy-clojars))
