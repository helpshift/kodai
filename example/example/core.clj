(ns example.core)

(defn hash-map? [x]
  (instance? clojure.lang.APersistentMap x))

(defn long? [x]
  (instance? Long x))
