(ns kodai.core
  (:require [kodai.bundle :as bundle]
            [kodai.core.viewer :as viewer]))

(defn insight
  "creates a viewer for the bundle"
  {:added "0.1"}
  ([] (insight [#"src"] {}))
  ([regexs] (insight regexs {}))
  ([regexs options]
   (let [bundle (bundle/bundle regexs)
         viewer (viewer/viewer {:bundle bundle}
                               options)]
     viewer)))
