(ns kodai.core.format
  (:require [clojure.string :as string]))

(defn format-label
  "formats label of according to specification
   (format-label 'x.y/z {:label {:type :full}})
   => x.y/z
 
   (format-label 'x.y/hello {:label {:type :name}})
   => hello
 
   (format-label 'x.y.z/hello {:label {:type :partial}})
   => y.z/hello
 
   (format-label 'x.y.z/hello {:label {:type :partial :skip 2}})
   => z/hello"
  {:added "0.1"}
  [var {:keys [label] :as modifiers}]
  (case (:type label)
    :full var
    :name (name var)
    :initials (str (->> (string/split (.getNamespace var) #"\.")
                        (map first)
                        (string/join "."))
                   "/" (name var))
    :partial  (let [{:keys [skip]} label]
                (str (->> (string/split (.getNamespace var) #"\.")
                         (drop (or skip 1))
                         (string/join "."))
                    "/" (name var)))))
