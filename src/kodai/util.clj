(ns kodai.util
  (:require [clojure.set :as set]
            [hara.data.nested :as nested]))

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

(defn namespace?
  ([var namespaces]
   (namespace? var namespaces symbol))
  ([var namespaces coerce]
   (get namespaces (coerce (.getNamespace var)))))

(defn keep-vars
  ([vars namespaces]
   (keep-vars vars namespaces symbol))
  ([vars namespaces coerce]
   (reduce (fn [out var]
             (if (namespace? var namespaces coerce)
               (conj out (coerce var))
               out))
           #{}
           vars)))

(defn drop-vars
  ([vars namespaces]
   (drop-vars vars namespaces symbol))
  ([vars namespaces coerce]
   (reduce (fn [out var]
             (if (namespace? var namespaces coerce)
               out
               (conj out (coerce var))))
           #{}
           vars)))

(defn collapse-namespaces
  [nodes namespaces]
  (reduce-kv (fn [out k v]
               (let [v (drop-vars v namespaces)]
                 (if (namespace? k namespaces)
                   (update-in out [(symbol (.getNamespace k))] (fnil #(set/union v %) #{}))
                   (assoc out k v))))
             {}
             nodes))

(defn keywordize-keys
  [m]
  (nested/update-keys-in m [] keyword))

(defn keywordize-links
  [m]
  (nested/update-vals-in m [] (fn [v] (set (map keyword v)))))

