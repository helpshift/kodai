(ns kodai.bundle-test
  (:require [kodai.bundle :as bundle]
            [gulfstream.core :as gs]
            [seesaw.core :as ui])
  (:import [javax.swing JFrame]))

(def bundle (bundle/bundle [#"src/gulfstream"]))

#_(defonce hara (bundle/bundle [#"src/hara"]))

(defonce kodai (bundle/bundle [#"src/kodai"]))

(defn select-vars
  ([graph entry]
   (select-vars graph entry {}))
  ([graph entry out]
   (let [adjacent (get graph entry)]
     (reduce (fn [out k]
               (if-let [val (get out k)]
                 out
                 (select-vars graph k
                              (assoc out k (get graph k)))))
             (assoc out entry adjacent)
             adjacent))))

(defn select-all-vars
  ([graph entries]
   (select-all-vars graph entries {}))
  ([graph [entry & more] out]
   (if entry
     (select-all-vars graph more (select-vars graph entry out))
     out)))

(defn visualize [bundle]
  (let [links (-> bundle
                :forward) 
      elements (->> links
                    keys
                    (map (juxt identity #(hash-map :label %)))
                    (into {}))]
    links
    (gs/display (gs/graph
                 {:dom (gs/expand {:links links
                                   :elements elements})}))))

(defn graph-ui [bundle]
  (let [links (-> bundle
                :forward) 
      elements (->> links
                    keys
                    (map (juxt identity #(hash-map :label %)))
                    (into {}))]
    (gs/view (gs/graph
                 {:dom (gs/expand {:links links
                                   :elements elements})}))))




(visualize {:forward (bundle/collapse-namespaces (:forward bundle) '#{gulfstream.graph.css
                                                                      gulfstream.graph.dom
                                                                      gulfstream.simulation.gen
                                                                      gulfstream.data.interop
                                                                      gulfstream.graph
                                                                      ;;gulfstream.core
                                                                      })})








(comment

  
  (ui/native!)
  (def view (ui/to-widget (graph-ui bundle)))
  (.* view #"setSize")

  (def f (JFrame. "Hello"))

  (.add (.getContentPane f) view)
  (.pack f)
  (.setVisible f true)
  
  (ui/width view)
  (ui/height view)
  (.getSize view)

  (.setSize view 100 100)
  (def f (ui/frame :title "Get to know Seesaw"
                   :size [640 :by 480]))
  (ui/config! f :content (ui/to-widget view))
  (.add f view)
  (.? f :name #"get")
  (map (comp seq #(.getComponents %)) (seq (.getComponents f)))

  (-> f ui/pack! ui/show!)
  (-> f ui/show!)
  (visualize bundle)

  (:forward bundle))








