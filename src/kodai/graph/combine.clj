(ns kodai.graph.combine
  (:require [clojure.set :as set]))

(defn remove-vars [graph vars]
  (let [ngraph (apply dissoc graph vars)]
    (reduce-kv (fn [out k v]
                 (assoc out k (set/difference v vars)))
               {}
               ngraph)))

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

