(ns kodai.graph.filter
  (:require [clojure.set :as set]))

(defn remove-vars [graph vars]
  (let [ngraph (apply dissoc graph vars)]
    (reduce-kv (fn [out k v]
                 (assoc out k (set/difference v vars)))
               {}
               ngraph)))
