(ns kodai.core.format
  (:require [clojure.string :as string]))

(defn format-label
  "formats label of according to specification
   (format-label 'x.y/z {:label :full})
   => \"x.y/z\"
 
   (format-label 'x.y/hello {:label :name})
   => \"hello\"
 
   (format-label 'x.y.z/hello {:label :partial})
   => \"y.z/hello\"
 
   (format-label 'x.y.z/hello {:label :partial :skip 2})
   => \"z/hello\""
  {:added "0.1"}
  [var {:keys [label skip] :as format}]
  (case label
    :full (str var)
    :name (name var)
    :initials (str (->> (string/split (.getNamespace var) #"\.")
                        (map first)
                        (drop (or skip 0))
                        (string/join "."))
                   "/" (name var))
    :partial  (str (->> (string/split (.getNamespace var) #"\.")
                        (drop (or skip 1))
              (string/join "."))
         "/" (name var))))
