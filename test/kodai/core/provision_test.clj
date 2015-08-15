(ns kodai.core.provision-test
  (:use midje.sweet)
  (:require [kodai.core.provision :refer :all]))

^{:refer kodai.core.provision/format-label :added "0.1"}
(fact "formats label of according to specification"
  (format-label 'x.y/z {:label {:type :full}})
  => "x.y/z"

  (format-label 'x.y/hello {:label {:type :name}})
  => "hello"

  (format-label 'x.y.z/hello {:label {:type :partial}})
  => "y.z/hello"

  (format-label 'x.y.z/hello {:label {:type :partial :skip 2}})
  => "z/hello")
