(ns kodai.core.provision
  (:require [clojure.string :as string]))

(defn format-label [var {:keys [label] :as modifiers}]
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

(defn provision-style [modifiers]
  (:style modifiers))

(defn provision-attributes [modifiers]
  (:attributes modifiers))

(defn dom-add-label [elements modifiers]
  (reduce-kv (fn [out k m]
               (assoc out k (assoc m :label (format-label k modifiers))))
             {}
             elements))

(defn dom-select-node [elements modifiers]
  (if-let [selected (-> modifiers :ui :selected)]
    (assoc-in elements [selected :ui.class] "selected")
    elements))

(defn dom-create-links [elements links]
  (reduce-kv (fn [elements source targets]
               (reduce (fn [elements target]
                         (update-in elements [[source target]] (fnil identity {})))
                       (update-in elements [source] (fnil identity {}))
                       targets))
             elements
             links))

(defn provision-dom [bundle modifiers]
  (let [links   (get bundle (-> modifiers :data :type))]
    (-> (zipmap (keys links) (repeat {}))
        (dom-add-label modifiers)
        (dom-select-node modifiers)
        (dom-create-links links))))
