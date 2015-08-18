(ns kodai.util-test
  (:use midje.sweet)
  (:require [kodai.util :refer :all]))

^{:refer kodai.util/reverse-graph :added "0.1"}
(fact "takes a call-graph and reverses the visulization of call"

  (def calls {:a #{:b :c :d}
              :b #{:c}
              :c #{:a}
              :d #{}})

  (reverse-graph calls)
  => {:a #{:c}, :d #{:a}, :b #{:a}, :c #{:b :a}}

  (-> calls reverse-graph reverse-graph)
  => calls)

^{:refer kodai.util/namespace? :added "0.1"}
(fact "figures out if the var is in one of the listed namespaces"

  (namespace? 'example.core/hello #{})
  => false

  (namespace? 'example.core/hello '#{example.core})
  => true)

^{:refer kodai.util/keep-vars :added "0.1"}
(fact "keeps the vars that are in the set of namespace"
  
  (keep-vars '#{x.y/a x.z/b} '#{x.y})
   => '#{x.y/a})

^{:refer kodai.util/keywordize-keys :added "0.1"}
(fact "modifies the keys of a map to be a keyword"

  (keywordize-keys {"a" 1 "b" 2})
  => {:b 2, :a 1})

^{:refer kodai.util/keywordize-links :added "0.1"}
(fact "modifies the keys of a map to be a keyword"

  (keywordize-links {"a" #{"c" "d"} "b" #{"e"}})
  => {"b" #{:e}, "a" #{:c :d}})
