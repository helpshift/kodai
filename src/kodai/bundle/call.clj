(ns kodai.bundle.call
  (:require [yagni.namespace.form :as form]
            [yagni.namespace :as namespace]
            [clojure.set :as set]))

(defn call-graph [namespaces]
  (let [graph      (do (namespace/prepare-namespaces namespaces)
                       (atom (namespace/named-vars-map namespaces)))
        _          (form/count-vars graph namespaces)]
    @graph))

(defn reverse-graph [graph]
  (let [rev (reduce-kv
             (fn [out k vs]
               (reduce (fn [out v]
                         (update-in out [v] (fnil #(conj % k) #{})))
                       out
                       vs))
             {} graph)
        ks  (set (keys graph))
        rks (set (keys rev))
        eks (set/difference ks rks)]
    (reduce (fn [out k]
              (assoc out k #{}))
            rev
            eks)))

(defn meta-info [symbol]
  (-> symbol
      resolve
      meta
      (update-in [:ns] #(.getName %))))
