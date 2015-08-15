(ns kodai.core.pipeline)

(defn find-dynamic-vars
  "gets a list of all dynamic vars within a set of elements"
  {:added "0.1"}
  [elements]
  (reduce-kv (fn [out k _]
               (if (#(and (.startsWith % "*")
                          (.endsWith % "*"))
                    (name k))
                 (conj out k)
                 out))
             #{}
             elements))

(defn pipe
  "a pipeline for manipulation of elements based upon specific options"
  {:added "0.1"}
  [elements options bundle])


