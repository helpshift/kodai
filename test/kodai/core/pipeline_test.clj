(ns kodai.core.pipeline-test
  (:use midje.sweet)
  (:require [kodai.core.pipeline :refer :all]
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


^{:refer kodai.core.pipeline/pipe :added "0.1"}
(fact "a pipeline for manipulation of elements based upon specific options:"
  
  {:reverse-calls     false  ; reverses call
   :hide-dynamic      true   ; 
   :hide-namespaces   #{}    ;
   :hide-singletons   true   ;
   :hide-vars         #{}    ;
   :select-namespaces #{}    ;
   :select-vars       #{}    ;
   :collapse-vars     #{}    ;
   })
