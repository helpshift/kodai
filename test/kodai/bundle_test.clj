(ns kodai.bundle-test
  (:use midje.sweet)
  (:require [kodai.bundle :refer :all]))

^{:refer kodai.bundle/pack :added "0.1"}
(fact "converts sniper forms into kodai compatible forms")

^{:refer kodai.bundle/keywordize-entries :added "0.1"}
(fact "converts the call graph into keyword entries for easier compatiblility 
  with graphstream"

  (keywordize-entries {:forward {"a" #{"b" "c"}}
                       :meta {"a" {"b" "c"}}})
  => {:reverse {},
      :meta {:a {"b" "c"}},
      :forward {:a #{:c :b}}})

^{:refer kodai.bundle/bundle :added "0.1"}
(fact "creates a bundle representing the call graph and associated metadata"
  (into {} (bundle #"example/example"))
  => {:forward {:example.core/keywordize #{:example.core/hash-map? :example.core/long?},
                :example.core/long? #{},
                :example.core/hash-map? #{}},
      :reverse {:example.core/keywordize #{},
                :example.core/long? #{:example.core/keywordize},
                :example.core/hash-map? #{:example.core/keywordize}},
      :meta {:example.core/hash-map? {:id :example.core/hash-map?,
                                      :end-line 4,
                                      :end-column 45,
                                      :column 1,
                                      :line 3,
                                      :ns "example.core"},
             :example.core/long? {:id :example.core/long?,
                                  :end-line 7,
                                  :end-column 22,
                                  :column 1,
                                  :line 6,
                                  :ns "example.core"},
             :example.core/keywordize {:id :example.core/keywordize,
                                       :end-line 14,
                                       :end-column 16,
                                       :column 1,
                                       :line 9,
                                       :ns "example.core"}}})
  

(comment (defonce hara (bundle/bundle [#"src/hara"]))

         (defonce kodai (bundle/bundle [#"src/kodai"]))

         (defn select-vars
           ([graph entry]
            (select-vars graph entry {}))
           ([graph entry out]
            (let [adjacent (get graph entry)]
              (reduce (fn [out k]
                        (if-let [val (get out k)]
                          out
                          (select-vars graph k
                                       (assoc out k (get graph k)))))
                      (assoc out entry adjacent)
                      adjacent))))

         (defn select-all-vars
           ([graph entries]
            (select-all-vars graph entries {}))
           ([graph [entry & more] out]
            (if entry
              (select-all-vars graph more (select-vars graph entry out))
              out)))







         )
