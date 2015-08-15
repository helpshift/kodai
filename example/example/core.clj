(ns example.core)

(defn hash-map? [x]
  (instance? clojure.lang.APersistentMap x))

(defn long? [x]
  (instance? Long x))

(defn keywordize [x]
  (cond (hash-map? x)
         :hash-map

        (long? x)
        :long))
