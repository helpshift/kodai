(ns kodai.util
  (:require [clojure.set :as set]
            [hara.data.nested :as nested]))

(defn reverse-graph
  "takes a call-graph and reverses the visulization of call
 
   (def calls {:a #{:b :c :d}
               :b #{:c}
               :c #{:a}
               :d #{}})
 
   (reverse-graph calls)
   => {:a #{:c}, :d #{:a}, :b #{:a}, :c #{:b :a}}
 
   (-> calls reverse-graph reverse-graph)
   => calls"
  {:added "0.1"}
  [graph]
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
  "figures out if the var is in one of the listed namespaces
 
   (namespace? 'example.core/hello #{})
   => false
 
   (namespace? 'example.core/hello '#{example.core})
   => true"
  {:added "0.1"}
  ([var namespaces]
   (namespace? var namespaces symbol))
  ([var namespaces coerce]
   (if (get namespaces (coerce (.getNamespace var)))
     true false)))

(defn keep-vars
  "keeps the vars that are in the set of namespace
   
   (keep-vars '#{x.y/a x.z/b} '#{x.y})
    => '#{x.y/a}"
  {:added "0.1"}
  ([vars namespaces]
   (keep-vars vars namespaces symbol))
  ([vars namespaces coerce]
   (reduce (fn [out var]
             (if (namespace? var namespaces coerce)
               (conj out (coerce var))
               out))
           #{}
           vars)))

(defn keywordize-keys
  "modifies the keys of a map to be a keyword
 
   (keywordize-keys {\"a\" 1 \"b\" 2})
   => {:b 2, :a 1}"
  {:added "0.1"}
  [m]
  (nested/update-keys-in m [] keyword))

(defn keywordize-links
  "modifies the keys of a map to be a keyword
 
   (keywordize-links {\"a\" #{\"c\" \"d\"} \"b\" #{\"e\"}})
   => {\"b\" #{:e}, \"a\" #{:c :d}}"
  {:added "0.1"}
  [m]
  (nested/update-vals-in m [] (fn [v] (set (map keyword v)))))
