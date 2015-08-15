(ns kodai.core.format-test
  (:use midje.sweet)
  (:require [kodai.core.format :refer :all]))

^{:refer kodai.core.format/format-label :added "0.1"}
(fact "formats label of according to specification"
  (format-label 'x.y/z {:label {:type :full}})
  => "x.y/z"

  (format-label 'x.y/hello {:label {:type :name}})
  => "hello"

  (format-label 'x.y.z/hello {:label {:type :partial}})
  => "y.z/hello"

  (format-label 'x.y.z/hello {:label {:type :partial :skip 2}})
  => "z/hello")
