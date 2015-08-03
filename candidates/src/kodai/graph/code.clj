(ns kodai.graph.code
  (:require [rewrite-clj.zip :as source]
            [jai.query :as query]
            [clojure.string :as string]))

(defn wrap-meta [f]
 (fn [source]
   (if (not= :meta (source/tag source))
     (f source)
     (f (-> source source/down source/right)))))

;; number of lines
;; def, defn or defmulti
(defn file-meta-map
 [file nsp]
 (let [zloc (source/of-file file)
       query  #(query/select zloc % {:return :zipper :walk :top})
       fnres  (query '[(#{defn defn-} _ ^:%?- string? ^:%?- map? ^:% vector? & _ )])
       vrres  (query '[def])
       get-lines (fn [zloc] (-> zloc
                                source/string
                                string/split-lines
                                count))
       down      (wrap-meta source/down)
       right     (wrap-meta source/right)
       get-name  (fn [zloc] (-> zloc down right source/string symbol))
       meta-list (fn [zlocs] (map (juxt get-name get-lines) zlocs))
       var-info  {:defn (meta-list fnres)
                  :def  (meta-list vrres)}]
   (reduce-kv (fn [out type metas]
                (reduce (fn [out [sym count]]
                          (assoc out (symbol (str nsp "/" sym))
                                 {:type type :line-count count}))
                        out
                        metas))
              {}
              var-info)))

(defn add-code [{:keys [filemap] :as bundle}]
  (reduce-kv (fn [bundle file nsp]
               (let [meta (file-meta-map file nsp)]
                 (update-in bundle [:calls :meta]
                            (partial merge-with merge)
                            meta)))
             bundle
             filemap))
