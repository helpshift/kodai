(ns kodai.core.pipeline
  (:require [clojure.set :as set]))

(defn remove-vars
  "removes all references of var from call graph
   
   (remove-vars {:a #{:b}
                 :b #{:a}
                 :c #{:a :b}} #{:a})
   => {:b #{}, :c #{:b}}"
  {:added "0.1"}
  [calls vars]
  (reduce-kv (fn [out k v]
               (if (vars k)
                 out
                 (assoc out k (set/difference v vars))))
             {}
             calls))

(defn pick-vars
  "picks only those references from the call graph
   
   (pick-vars {:a #{:b}
               :b #{:a}
               :c #{:a :b}} #{:a :b})
   => {:a #{:b}, :b #{:a}}"
  {:added "0.1"}
  [calls vars]
  (reduce-kv (fn [out k v]
               (if (vars k)
                 (assoc out k (set/intersection v vars))
                 out))
             {}
             calls))

(defn find-adjacent
  "returns a set of all functions adjacent to var
   
   (find-adjacent {:a #{:b :c} :b #{}}
                  :a)
   => #{:c :b}"
  {:added "0.1"}
  [calls var]
  (get calls var #{}))

(defn find-downstream
  "helper function for find-upstream and find-downstream
 
   (find-downstream {:a #{:b :c} :b #{} :c #{:d} :d #{}}
                    :a)
   => #{:c :b}"
  {:added "0.1"}
  ([calls var]
   (find-downstream calls var #{}))
  ([calls var out]
   (if-not (get out var)
     (let [entries (get calls var)]
       (reduce (fn [out entry]
                 (find-downstream calls entry out))
               (-> out (set/union entries))
               entries))
     out)))

(defn find-namespace-vars
  "returns vars that are in a particular namespace
 
   (find-namespace-vars {:x/b #{}
                         :y/c #{}
                         :z/a #{}}
                        #{\"x\" \"z\"})
   => #{:x/b :z/a}"
  {:added "0.1"}
  [calls namespaces]
  (reduce (fn [out k]
            (if (get namespaces (.getNamespace k))
              (conj out k)
              out))
          #{}
          (keys calls)))

(defn find-singletons
  "returns a set a isolated nodes
   
   (find-singletons {:a #{}
                     :b #{}
                     :c #{:a}})
   => #{:b}"
  {:added "0.1"}
  [calls]
  (let [potentials (reduce-kv (fn [out k v]
                                (if (empty? v)
                                  (conj out k)
                                  out))
                              #{} calls)]
    (reduce-kv (fn [out k v]
                 (set/difference potentials v))
               potentials
               calls)))

(defn find-dynamic
  "returns a set of all dynamic vars within a call graph
 
   (find-dynamic {:*hello* #{}})
   => #{:*hello*}"
  {:added "0.1"}
  [calls]
  (reduce-kv (fn [out k _]
               (if (#(and (.startsWith % "*")
                          (.endsWith % "*"))
                    (name k))
                 (conj out k)
                 out))
             #{}
             calls))

(defn find-downstream-vars
  "returns a set of all dynamic vars within a call graph
 
   (find-downstream-vars {:a #{}
                          :b #{}
                          :c #{:a}}
                         #{:c :b})
   => #{:a}"
  {:added "0.1"}
  [calls vars]
  (reduce (fn [out v]
            (find-downstream calls v out))
          #{}
          vars))

(defn meta-pipe
  [calls meta opts]
  )

(defn call-pipe
  "a pipeline for manipulation of elements based upon specific options:
   
   {:reverse-calls     false  ;
    :hide-dynamic      true   ; 
    :hide-namespaces   #{}    ;
    :hide-singletons   true
    :hide-vars         #{}
    :select-namespaces #{}
    :select-vars       #{}
    :collapse-vars     #{}}
   "
  {:added "0.1"}
  [bundle opts]
  (let [;; reverse-calls
        calls (if (:reverse-calls opts)
                (:reverse bundle)
                (:forward bundle))

        ;; select-vars - find-downstream-vars (union entries) pick-vars
        calls (if (empty? (:select-vars opts))
                calls
                (->> (:select-vars opts)
                     (find-downstream-vars calls)
                     (set/union (:select-vars opts))
                     (pick-vars calls)))

        ;; select-namespaces - find-namespace-vars pick-vars
        calls (if (empty? (:select-namespaces opts))
                calls
                (->> (:select-namespaces opts)
                     (find-namespace-vars calls)
                     (pick-vars calls)))

        ;; collapse-vars - find-downstream-vars remove-vars
        calls (if (empty? (:collapse-vars opts))
                calls
                (->> (:collapse-vars opts)
                     (find-downstream-vars calls)
                     (remove-vars calls)))

        ;; hide-namespaces - find-namespace-vars remove-vars
        calls (if (empty? (:hide-namespaces opts))
                calls
                (->> (:hide-namespaces opts)
                     (find-namespace-vars calls)
                     (remove-vars calls)))

        ;; hide-vars - remove-vars
        calls (if (empty? (:hide-vars opts))
                calls
                (remove-vars calls (:hide-vars opts)))

        ;; hide-singletons - find-singletons remove-vars
        calls (if (:hide-singletons opts)
                (->> (find-singletons calls)
                     (remove-vars calls))
                calls)

        ;; hide-dynamic - find-dynamic remove-vars
        calls (if (:hide-dynamic opts)
                (->> (find-dynamic calls)
                     (remove-vars calls))
                calls)]
    calls))
