(ns kodai.graph.combine-test
  (:require [kodai.graph :as graph]
            [kodai.graph.combine :as combine]
            [gulfstream.core :as gs]
            [gulfstream.graph :as stream]))


(comment
  (defonce bundle (graph/project-info ["../../chit/hara/src"]))

  (keys bundle)
  
  ;; (:calls :namespaces :files :deps :unload :filemap :time)
  (count (combine/select-vars (-> bundle :calls :forward)
                              'hara.reflect.core.query/query-hierarchy))
  
  (count (combine/select-vars (-> bundle :calls :forward)
                              'hara.reflect.core.class/class-info))
  
  (count (combine/select-all-vars (-> bundle :calls :forward)
                                  '[hara.reflect.core.class/class-info
                                    hara.reflect.core.query/query-hierarchy]))
  
  
  (gs/display (gs/graph {:dom (gs/expand
                               {:links
                                (combine/select-all-vars
                                 (-> bundle :calls :reverse)
                                 '[hara.reflect.core.class/class-info
                                   hara.reflect.core.query/query-hierarchy])})}))

  (gs/display (gs/graph {:dom (gs/expand
                               {:links
                                (combine/select-all-vars
                                 (-> bundle :calls :forward)
                                 '[hara.reflect.core.class/class-info
                                   ])})}))

  (defn prep-vars [bundle vars]
    (let [links (combine/select-all-vars
                 (-> bundle :calls :forward)
                 vars)
          elements (->> links
                        keys
                        (map (juxt identity (fn [f] {:label f})))
                        (into {}))]
      {:dom (gs/expand {:links links
                        :elements elements})}))

  (stream/dom gs/*current-graph*
              (:dom (prep-vars bundle '[hara.reflect.core.class/class-info
                                        hara.reflect.core.query/query-hierarchy]))
              )

  (gs/display gs/*current-graph*)

  

  (draw-vars bundle '[hara.reflect.core.class/class-info])
  ;; (keys (:calls bundle))
  
  ;; (def bundle *1)
  )
