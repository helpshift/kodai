(ns kodai.visualise
  (:require [kodai.bundle :as bundle]
            [gulfstream.core :as graph]
            [gulfstream.graph :refer [dom]]
            [gulfstream.graph.dom :as dom]
            [clojure.string :as string])
  (:import java.awt.event.KeyEvent))

(defrecord Application [])

(defmethod print-method Application
  [v w]
  (.write w (str (into {} v))))

(def +default-modifiers+
  {:style [[:node.selected {:fill-color "green"
                            :size "30px"}]]
   :label {:type :partial
           :skip 1}
   :ui   {:selected nil
          :chords #{}}
   :data {:type :forward
          :select #{}
          :collapse #{}}})

(defn label [id {:keys [label] :as modifiers}]
  (case (:type label)
    :full id
    :name (name id)
    :partial (let [{:keys [skip]} label]
               (str (->> (string/split (.getNamespace id) #"\.")
                         (drop skip)
                         (string/join "."))
                    "/" (name id)))))

(defn provision [bundle modifiers]
  (let [links    (get bundle (-> @modifiers :data :type)) 
        elements (->> links
                      keys
                      (map (juxt identity #(hash-map :label (label % @modifiers))))
                      (into {}))
        elements (if-let [selected (-> @modifiers :ui :selected)]
                   (do (prn "SELECTED:" selected)
                       (assoc-in elements [selected :ui.class] "selected"))
                   elements)]
    (graph/expand {:links links
                   :elements elements})))

(defn visualise [regexs]
  (let [bundle    (-> (bundle/bundle regexs)
                      (update-in [:forward] #(-> %
                                                 (hara.data.nested/update-keys-in [] keyword)
                                                 (hara.data.nested/update-vals-in [] (fn [v] (set (map keyword v))))))
                      (update-in [:reverse] #(-> %
                                                 (hara.data.nested/update-keys-in [] keyword)
                                                 (hara.data.nested/update-vals-in [] (fn [v] (set (map keyword v))))))
                      (update-in [:meta] hara.data.nested/update-keys-in [] keyword))
        modifiers (atom +default-modifiers+)
        dom       (atom (provision bundle modifiers))
        graph     (graph/graph {:dom @dom
                                :style [[:node.selected {:size "20px"
                                                         :fill-color "green"}]]})
        viewer    (-> (graph/display graph)
                      (graph/add-viewer-listener
                       {:on-push (fn [id]
                                   (swap! modifiers update-in [:ui] assoc :selected (keyword id)))})
                      (graph/add-key-listener
                       {:on-push (fn [e]
                                   (let [k (keyboard (.getKeyCode e))]
                                     (cond (= k :escape) 
                                           (swap! modifiers update-in [:ui] assoc :selected nil)

                                           (#{:shift :control :meta :alt} k)
                                           (swap! modifiers update-in [:ui :chords] conj k))))
                        :on-release (fn [e]
                                      (let [k (keyboard (.getKeyCode e))]
                                        (cond (#{:shift :control :meta :alt} k)
                                              (swap! modifiers update-in [:ui :chords] disj k))))}))
        _         (add-watch modifiers :update-dom
                             (fn [_ _ _ v]
                               (dom/set-dom graph (provision bundle modifiers))))]
    
    (map->Application
     {:bundle bundle
      :dom    dom
      :graph  graph
      :viewer (atom viewer)
      :modifiers  modifiers})))

(comment
  (def regexs #"src/gulfstream")
  
  (def app (visualise regexs))

  (dom/get-dom (:graph grph))

  (first (-> app :bundle :forward))
  (first (-> app :bundle :meta))
  [:gulfstream.graph.css/rule-pair
   {:calls #{:gulfstream.graph.css/property-pair},
    :id :gulfstream.graph.css/rule-pair,
    :end-line 25, :end-column 12, :column 1, :line 13, :ns :gulfstream.graph.css, :file "file:/Users/chris/Development/helpshift/gulfstream/src/gulfstream/graph/css.clj"}]
  [gulfstream.core/view {:calls #{},
                         :id gulfstream.core/view,
                         :end-line 65,
                         :end-column 13,
                         :column 1,
                         :line 60,
                         :ns gulfstream.core,
                         :file "file:/Users/chris/Development/helpshift/gulfstream/src/gulfstream/core.clj"}]
  )
