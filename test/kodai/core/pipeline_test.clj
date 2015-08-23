(ns kodai.core.pipeline-test
  (:use midje.sweet)
  (:require [kodai.core.pipeline :refer :all]
            [kodai.core.viewer :as viewer]
            [kodai.bundle :as bundle]))

^{:refer kodai.core.pipeline/remove-vars :added "0.1"}
(fact "removes all references of var from call graph"

  (remove-vars {:a #{:b}
                :b #{:a}
                :c #{:a :b}} #{:a})
  => {:b #{}, :c #{:b}})

^{:refer kodai.core.pipeline/pick-vars :added "0.1"}
(fact "picks only those references from the call graph"

  (pick-vars {:a #{:b}
              :b #{:a}
              :c #{:a :b}} #{:a :b})
  => {:a #{:b}, :b #{:a}})

^{:refer kodai.core.pipeline/find-adjacent :added "0.1"}
(fact "returns a set of all functions adjacent to var"

  (find-adjacent {:a #{:b :c} :b #{}}
                 :a)
  => #{:c :b})

^{:refer kodai.core.pipeline/find-downstream :added "0.1"}
(fact "helper function for find-upstream and find-downstream"

  (find-downstream {:a #{:b :c} :b #{} :c #{:d} :d #{}}
                   :a)
  => #{:c :b})

^{:refer kodai.core.pipeline/find-namespace-vars :added "0.1"}
(fact "returns vars that are in a particular namespace"

  (find-namespace-vars {:x/b #{}
                        :y/c #{}
                        :z/a #{}}
                       #{"x" "z"})
  => #{:x/b :z/a})

^{:refer kodai.core.pipeline/find-singletons :added "0.1"}
(fact "returns a set a isolated nodes"

  (find-singletons {:a #{}
                    :b #{}
                    :c #{:a}})
  => #{:b})

^{:refer kodai.core.pipeline/find-dynamic :added "0.1"}
(fact "returns a set of all dynamic vars within a call graph"

  (find-dynamic {:*hello* #{}})
  => #{:*hello*})

^{:refer kodai.core.pipeline/find-downstream-vars :added "0.1"}
(fact "returns a set of all dynamic vars within a call graph"

  (find-downstream-vars {:a #{}
                         :b #{}
                         :c #{:a}}
                        #{:c :b})
  => #{:a})

^{:refer kodai.core.pipeline/call-pipe :added "0.1"}
(fact "a pipeline for manipulation of elements based upon specific options:"

  (-> (bundle/bundle #"example")
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
      :example.core/hash-map? #{}})

^{:refer kodai.core.pipeline/css-string :added "0.1"}
(fact "creates a css-string from a clojure one"

  (css-string "clojure.core/add")
  => "clojure_core__add")

 ^{:refer kodai.core.pipeline/elements-pipe :added "0.1"}
(fact "creates elements from a call graph for display as dom elements"

  (-> (bundle/bundle #"example")
      (call-pipe viewer/+default-options+)
      (elements-pipe viewer/+default-options+))
  => {:nodes {:example.core/keywordize
              {:full :example.core/keywordize,
               :label "c/keywordize",
               :namespace "example.core",
               :ui.class ["ns_example_core"]},
              :example.core/long?
              {:full :example.core/long?,
               :label "c/long?",
               :namespace "example.core",
               :ui.class ["ns_example_core"]},
              :example.core/hash-map?
              {:full :example.core/hash-map?,
               :label "c/hash-map?",
               :namespace "example.core",
               :ui.class ["ns_example_core"]}},
      :edges {[:example.core/keywordize :example.core/hash-map?]
              {:from :example.core/keywordize,
               :to :example.core/hash-map?,
               :from-ns "example.core",
               :to-ns "example.core",
               :ui.class ["to_example_core" "from_example_core"]},
              [:example.core/keywordize :example.core/long?]
              {:from :example.core/keywordize,
               :to :example.core/long?,
               :from-ns "example.core",
               :to-ns "example.core",
               :ui.class ["to_example_core" "from_example_core"]}}})
