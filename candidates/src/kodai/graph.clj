(ns kodai.graph
  (:require [kodai.graph.base :as base]
            [kodai.graph.call :as call]
            [kodai.graph.code :as code]))


;; for every symbol:

;; - namespace
;; - is it a def or a defn
;; - is it earmuffed
;; - private?
;; - how many lines

;; count connections:

;; - how many inputs are going in
;; - how many outputs are coming out
;; - namespaces internally
;; - namespaces going in
;; - namespaces going out

(defn project-info
  [source-paths]
  (let [bundle (base/source-info source-paths)]
    (-> bundle
        (call/add-calls)
        (code/add-code))))

(defn dependency-tree
  ([graph entry]
   (dependency-tree graph entry {}))
  ([graph entry out]
   (let [adjacent (get graph entry)]
     (reduce (fn [out k]
               (if-let [val (get out k)]
                 out
                 (dependency-tree graph k
                                  (assoc out k (get graph k)))))
             (assoc out entry adjacent)
             adjacent))))

(defn dependency-network
  [graph entries]
  (reduce (fn [out entry]
            (dependency-tree graph entry out))
          {}
          entries))

(comment
  (file-meta-map (.getPath (first (keys (:filemap trk)))) 'kodai.graph)

  (group-by-namespace (keys (project-graph ["src"])))


  (query/$ (.getPath (first (keys (:filemap trk))))
           [(defn | _ ^:%?- string? ^:%?- map? ^:% vector? & _ )] {:return :string :walk :top})


  (defn- helo "oeuoe" {:ahh "oeue"} [] 2)

  (def bundle (project-info ["src"]))
  (def bundle (project-info ["example"]))

  (dependency-tree (-> bundle :calls :reverse) 'example.core/long?)


  (-> bundle :calls :meta)
  (type (project-info ["src"]))

  (map count (map clojure.string/split-lines
                  (query/$ (.getPath (ffirst (:clojure.tools.namespace.file/filemap trk)))
                           [(defn _ ^:%?- string? ^:%?- map? ^:% vector? & _ )] {:return :string :walk :top})))

  (./source var) (var helo)

  (meta (resolve 'helo))

  (defn a
    []
    (b 1))

  (defn b
    []
    (a 1))

  (defn c []
    (a (b 1))))
;;
