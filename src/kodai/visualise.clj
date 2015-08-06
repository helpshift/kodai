(ns kodai.visualise
  (:require [kodai.bundle :as bundle]
            [gulfstream.core :as graph])
  (:import java.awt.event.KeyEvent))

(defrecord Application [])

(defmethod print-method Application
  [v w]
  (.write w (str (into {} v))))

(defonce +default-modifiers+
  {:style {}
   :ui   {:selected nil}
   :data {:type :forward
          :select #{}
          :shrink #{}
          :collapse #{}}})

(defn meta->attributes [])

(defn prepare)

(defn provision [bundle modifiers])

(let [links    (get bundle (-> @modifiers :data :type)) 
      elements (->> links
                    keys
                    (map (juxt identity #(hash-map :label %)))
                    (into {}))]
    (gs/view (gs/graph
                 {:dom (gs/expand {:links links
                                   :elements elements})})))

(defn visualise [regexs]
  (let [bundle    (bundle/bundle regexs)
        modifiers (atom +default-modifiers+)
        graph     (graph/graph {:dom (graph/expand {:links (get bundle (-> @modifiers :data :type))})})
        viewer    (-> (graph/display graph)
                      (graph/add-viewer-listener
                       {:on-push (fn [id]
                                   (swap! modifiers update-in [:ui] assoc :selected (keyword id)))})
                      (graph/add-key-listener
                       {:on-push (fn [e]
                                   (case (.getKeyCode e)
                                     KeyEvent/VK_ESCAPE (swap! modifiers update-in [:ui] assoc :selected nil)
                                     nil))}))
        _         (add-watch modifiers :println
                             (fn [_ _ _ v]
                               (println "Selected Node:" (get-in v [:ui :selected]))))]
    (map->Application
     {:bundle bundle
      :graph  graph
      :viewer (atom viewer)
      :modifiers  modifiers})))

(comment
  (def regexs #"src/gulfstream")
  (def bundle (bundle/bundle regexs))
  (def grph (visualise #"src/gulfstream"))

  (:modifiers grph
              )
  
  )
