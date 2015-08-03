(ns kodai.bundle
  (:require [kodai.bundle
             [common :as common]
             [call :as call]
             [code :as code]]))

(defn add-code [{:keys [filemap] :as bundle}]
  (reduce-kv (fn [bundle file nsp]
               (let [meta (file-meta-map file nsp)]
                 (update-in bundle [:calls :meta]
                            (partial merge-with merge)
                            meta)))
             bundle
             filemap))

(defn add-calls [{:keys [namespaces] :as bundle}]
 (let [forward    (call-graph namespaces)
       funcs      (set (keys forward))
       reverse    (reverse-graph forward)
       meta       (zipmap funcs (map meta-info funcs))]
   (-> bundle
       (assoc :calls {:forward forward
                      :reverse reverse
                      :meta    meta}))))

(defn bundle
  [source-paths]
  (let [bundle (common/source-info source-paths)]
    (-> bundle
        (call/add-calls)
        (code/add-code))))
