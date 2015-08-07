(ns kodai.graph-test
  (:require [gulfstream.core :as graph]))

(graph/expand {:links {:a #{:b :c}
                       :b #{}
                       :c #{}}
               :elements {[:a :b] {:class "arrow"}
                          [:a :c] {:class "arrow"}}})

(-> (graph/graph 
     {:dom {:c {},
            :b {},
            :a {:ui.class ["selected"]},
            [:a :b] {:ui.class ["arrow"]},
            [:a :c] {:ui.class "arrow"}}
      :style [[:node.selected
               {:size "30px"}]]})
    (graph/display))


{:c {},
 :b {},
 [:a :c] {},
 :a {},
 [:a :b] {:class "arrow"}}
