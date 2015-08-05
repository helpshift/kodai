(ns kodai.bundle-test
  (:require [kodai.bundle :as bundle]
            [gulfstream.core :as gs]
            [seesaw.core :as ui]))

(def bundle (bundle/bundle [#"src/gulfstream"]))

#_(defonce hara (bundle/bundle [#"src/hara"]))

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

(defn visualize [bundle]
  (let [links (-> bundle
                :forward) 
      elements (->> links
                    keys
                    (map (juxt identity #(hash-map :label %)))
                    (into {}))]
    links
    (gs/display (gs/graph
                 {:dom (gs/expand {:links links
                                   :elements elements})}))))

(visualize bundle)

(:forward bundle)








