(ns kodai.core.pipeline-test
  (:use midje.sweet)
  (:require [kodai.core.pipeline :refer :all]))

^{:refer kodai.core.pipeline/find-dynamic-vars :added "0.1"}
(fact "gets a list of all dynamic vars within a set of elements")

^{:refer kodai.core.pipeline/pipe :added "0.1"}
(fact "a pipeline for manipulation of elements based upon specific options")
