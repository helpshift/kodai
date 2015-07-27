(ns kodai.graph.base
  (:require  [clojure.tools.namespace.dir :as dir]
             [clojure.tools.namespace.track :as track]
             [clojure.string :as string]
             [clojure.java.io :as io]
             [hara.data.nested :as nested]))

(defrecord Bundle []
  Object
  (toString [obj]
    (str "#bundle" (vec (keys obj)))))

(defmethod print-method Bundle [v w]
  (.write w (str v)))

(defn canonical-path [path]
  (.getCanonicalPath (io/file path)))

(defn source-info
  [source-paths]
  (let [dirs   (map canonical-path source-paths)
        bundle (if (seq dirs)
                 (apply dir/scan-all (track/tracker) dirs)
                 (track/tracker))
        bundle (nested/update-keys-in bundle [] (comp keyword name))]
    (-> bundle
        (assoc :namespaces (:load bundle))
        (dissoc :load)
        (map->Bundle))))
