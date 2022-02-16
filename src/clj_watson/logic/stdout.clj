(ns clj-watson.logic.stdout
  (:require
   [cljstache.core :refer [render]]
   [clojure.edn :as edn]))

(def ^:private template-path "report.mustache")

(defn ^:private dependencies-hierarchy-to-tree* [tree]
  (loop [text ""
         count 0
         dependencies tree]
    (if (nil? dependencies)
      text
      (let [tabs (apply str (repeat count "\t"))
            dependency (first dependencies)
            new-text (format "%s%s[%s]\n" text tabs dependency)]
        (recur new-text (inc count) (next dependencies))))))

(defn ^:private dependencies-hierarchy-to-tree [tree-text dependency-name]
  (fn [render-fn]
    (let [trees (some-> tree-text render-fn edn/read-string)]
      (if (seq trees)
        (->> trees
             reverse
             (map dependencies-hierarchy-to-tree*)
             (reduce #(str %1 "\n" %2)))
        (format "[%s]" dependency-name)))))

(defn generate [dependencies template]
  (render template (assoc dependencies :build-tree dependencies-hierarchy-to-tree)))