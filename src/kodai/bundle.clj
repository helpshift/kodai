(ns kodai.bundle
  (:require [sniper.snarf :as analyser]
            [clojure.set :as set]
            [hara.data.nested :as nested]
            [kodai.util :as util]))

(defrecord Bundle [])

(defmethod print-method Bundle [v w]
  (.write w (str "#bundle" (vec (keys v)))))

(defn pack [forms]
  (->> forms
       (mapcat (fn [{:keys [source-info var-defs var-refs]}]
                 (map #(assoc source-info
                              :id %
                              :calls (set var-refs))
                      var-defs)))
       (map (juxt :id identity))
       (into {})))

(defn keywordize-entries [bundle]
  (let [all-fn (comp util/keywordize-keys util/keywordize-links)]
    (-> bundle
        (update-in [:forward] all-fn)
        (update-in [:reverse] all-fn)
        (update-in [:meta] util/keywordize-keys))))

(defn bundle [regexs]
  (let [regexs (if (vector? regexs) regexs [regexs])
        vars (-> (apply analyser/classpath-ns-forms regexs)
                 pack)
        namespaces (set (map :ns (vals vars)))
        meta (reduce-kv (fn [out k v]
                          (if (util/namespace? k namespaces)
                            (update-in out [k :calls] util/keep-vars namespaces)
                            (dissoc out k)))
                        vars
                        vars)
        forward (reduce-kv (fn [out k v]
                             (assoc out k (:calls v)))
                           {}
                           meta)
        reverse (util/reverse-graph forward)
        bundle  (map->Bundle {:forward forward
                              :reverse reverse
                              :meta meta})]
    (keywordize-entries bundle)))

(comment
  (def vs (analyser/classpath-ns-forms #"src/gulfstream"))
  
  
  (first (:forward (keywordize-bundle (bundle #"src/gulfstream"))))
  
  (.getNamespace 'hoeu.oeu/oeu)

  (get (summarize (transform vs))
       'gulfstream.graph.dom/diff))
