(ns kodai.core.pipeline
  (:require [clojure.set :as set]
            [kodai.core.format :as format]))

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
   => #{:c :b :d}"
  {:added "0.1"}
  ([calls var]
   (find-downstream calls var #{} #{}))
  ([calls var analysed out]
   (if (get analysed var)
     out
     (let [entries (get calls var)]
       (reduce (fn [out entry]
                 (find-downstream calls entry (conj analysed var) out))
               (-> out (set/union entries))
               entries)))))

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
    (set/difference potentials
                    (apply set/union (vals calls)))))

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
  (->> vars
       (map #(find-downstream calls %))
       (apply set/union)))

(defn call-pipe
  "a pipeline for manipulation of elements based upon specific options:

   (-> (bundle/bundle #\"example\")
       (call-pipe {:bundle {:reverse-calls     false
                            :hide-dynamic      false
                            :hide-namespaces   #{}
                            :hide-singletons   false
                            :hide-vars         #{}
                            :select-namespaces #{}
                           :select-vars       #{}
                            :collapse-vars     #{}}}))
   => {:example.core/keywordize #{:example.core/hash-map? :example.core/long?},
       :example.core/long? #{},
       :example.core/hash-map? #{}}"
  {:added "0.1"}
  [bundle {opts :bundle}]
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

(defn css-string
  "creates a css-string from a clojure one

   (css-string \"clojure.core/add\")
   => \"clojure_core__add\""
  {:added "0.1"}
  [s]
  (-> s
      (.replaceAll "\\." "_")
      (.replaceAll "/" "__")))

(defn elements-pipe
  "creates elements from a call graph for display as dom elements

   (-> (bundle/bundle #\"example\")
       (call-pipe viewer/+default-options+)
       (elements-pipe viewer/+default-options+))
   => {:nodes {:example.core/keywordize
               {:full :example.core/keywordize,
                :label \"c/keywordize\",
               :namespace \"example.core\",
                :ui.class [\"ns_example_core\"]},
               :example.core/long?
               {:full :example.core/long?,
                :label \"c/long?\",
                :namespace \"example.core\",
                :ui.class [\"ns_example_core\"]},
               :example.core/hash-map?
               {:full :example.core/hash-map?,
                :label \"c/hash-map?\",
                :namespace \"example.core\",
                :ui.class [\"ns_example_core\"]}},
       :edges {[:example.core/keywordize :example.core/hash-map?]
               {:from :example.core/keywordize,
                :to :example.core/hash-map?,
                :from-ns \"example.core\",
                :to-ns \"example.core\",
                :ui.class [\"to_example_core\" \"from_example_core\"]},
               [:example.core/keywordize :example.core/long?]
               {:from :example.core/keywordize,
                :to :example.core/long?,
                :from-ns \"example.core\",
                :to-ns \"example.core\",
                :ui.class [\"to_example_core\" \"from_example_core\"]}}}"
  {:added "0.1"}
  [calls opts]
  (let [collapsed   (-> opts :bundle :collapse-vars)
        selected    (-> opts :bundle :select-vars)
        highlighted (-> opts :bundle :highlight-vars)
        v-fn  #(vec (filter identity %))
        nodes (->> (keys calls)
                   (map (juxt identity (fn [v] (let [ns (.getNamespace v)]
                                                 {:full v
                                                  :label (format/format-label v (:format opts))
                                                  :namespace ns
                                                  :ui.class
                                                  (v-fn [(str "ns_" (css-string ns))
                                                         (if (collapsed v) "collapsed")
                                                         (if (selected v) "selected")
                                                         (if (highlighted v) "highlighted")])}))))
                   (into {}))
        edges (->> (seq calls)
                   (mapcat (fn [[from tos]]
                             (map (fn [to]
                                    [[from to]
                                     (let [from-ns (get-in nodes [from :namespace])
                                           to-ns   (get-in nodes [to :namespace])]
                                       {:from from
                                        :to to
                                        :from-ns from-ns
                                        :to-ns to-ns
                                        :ui.class [(str "to_"   (css-string to-ns))
                                                   (str "from_" (css-string from-ns))]})])
                                  tos)))
                   (into {}))]
    {:nodes nodes
     :edges edges}))
