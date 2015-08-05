(ns kodai.bundle
  (:require [sniper.snarf :as analyser]
            [clojure.set :as set]))

(defrecord Bundle []
  Object
  (toString [obj]
    (str "#bundle" (vec (keys obj)))))

(defmethod print-method Bundle [v w]
  (.write w (str v)))

(defn reverse-graph [graph]
  (let [rev (reduce-kv
             (fn [out k vs]
               (reduce (fn [out v]
                         (update-in out [v] (fnil #(conj % k) #{})))
                       out
                       vs))
             {} graph)
        ks  (set (keys graph))
        rks (set (keys rev))
        eks (set/difference ks rks)]
    (reduce (fn [out k]
              (assoc out k #{}))
            rev
            eks)))

(defn pack [forms]
  (->> forms
       (mapcat (fn [{:keys [source-info var-defs var-refs]}]
                 (map #(assoc source-info
                              :id %
                              :calls (set var-refs))
                      var-defs)))
       (map (juxt :id identity))
       (into {})))

(defn namespace? [var namespaces]
  (get namespaces (symbol (.getNamespace var))))

(defn keep-vars [vars namespaces]
  (reduce (fn [out var]
            (if (namespace? var namespaces)
              (conj out var)
              out))
          #{}
          vars))

(defn bundle [regexs]
  (let [regexs (if (vector? regexs) regexs [regexs])
        vars (-> (apply analyser/classpath-ns-forms regexs)
                 pack)
        namespaces (set (map :ns (vals vars)))
        meta (reduce-kv (fn [out k v]
                          (if (namespace? k namespaces)
                            (update-in out [k :calls] keep-vars namespaces)
                            (dissoc out k)))
                        vars
                        vars)
        forward (reduce-kv (fn [out k v]
                             (assoc out k (:calls v)))
                           {}
                           meta)
        reverse (reverse-graph forward)]
    (map->Bundle {:forward forward
                  :reverse reverse
                  :meta meta})))

(comment
  (def vs (analyser/classpath-ns-forms #"src/gulfstream"))
  
  

  (bundle #"src/gulfstream")
  
  (.getNamespace 'hoeu.oeu/oeu)

  (get (summarize (transform vs))
       'gulfstream.graph.dom/diff))
