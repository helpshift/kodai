(ns kodai.bundle
  (:require [sniper.snarf :as analyser]
            [clojure.set :as set]
            [hara.data.nested :as nested]
            [kodai.util :as util]))

(defrecord Bundle [])

(defmethod print-method Bundle [v w]
  (.write w (str "#bundle" (vec (keys v)))))

(defn pack
  "converts sniper forms into kodai compatible forms"
  {:added "0.1"}
  [forms]
  (->> forms
       (mapcat (fn [{:keys [source-info var-defs var-refs]}]
                 (map #(assoc source-info
                              :id %
                              :calls (set var-refs))
                      var-defs)))
       (map (juxt :id identity))
       (into {})))

(defn keywordize-entries
  "converts the call graph into keyword entries for easier compatiblility 
   with graphstream
 
   (keywordize-entries {:forward {\"a\" #{\"b\" \"c\"}}
                        :meta {\"a\" {:id \"hello\" :ns 'clojure.core}}})
   => {:reverse {},
       :meta {:a {:ns \"clojure.core\", :id :hello}},
       :forward {:a #{:c :b}}}"
  {:added "0.1"}
  [bundle]
  (let [all-fn (comp util/keywordize-keys util/keywordize-links)
        elem-fn (fn [m]
                  (-> m
                      (update-in [:id] keyword)
                      (update-in [:ns] name)
                      (dissoc :file :calls)))]
    (-> bundle
        (update-in [:forward] (fnil all-fn {}))
        (update-in [:reverse] (fnil all-fn {}))
        (update-in [:meta] #(-> %
                                util/keywordize-keys
                                (nested/update-vals-in [] elem-fn))))))

(defn bundle
  "creates a bundle representing the call graph and associated metadata
   (into {} (bundle #\"example/example\"))
   => {:forward {:example.core/keywordize #{:example.core/hash-map? :example.core/long?},
                 :example.core/long? #{},
                 :example.core/hash-map? #{}},
       :reverse {:example.core/keywordize #{},
                 :example.core/long? #{:example.core/keywordize},
                 :example.core/hash-map? #{:example.core/keywordize}},
      :meta {:example.core/hash-map? {:id :example.core/hash-map?,
                                       :end-line 4,
                                       :end-column 45,
                                       :column 1,
                                       :line 3,
                                       :ns \"example.core\"},
              :example.core/long? {:id :example.core/long?,
                                   :end-line 7,
                                   :end-column 22,
                                   :column 1,
                                   :line 6,
                                   :ns \"example.core\"},
              :example.core/keywordize {:id :example.core/keywordize,
                                        :end-line 14,
                                        :end-column 16,
                                        :column 1,
                                        :line 9,
                                        :ns \"example.core\"}}}"
  {:added "0.1"}
  [regexs]
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
