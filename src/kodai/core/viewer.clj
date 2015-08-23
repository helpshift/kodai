(ns kodai.core.viewer
  (:require [gulfstream.core :as gs]
            [hara.concurrent.latch :as latch]
            [hara.data.nested :as nested]
            [hara.common.watch :as watch]
            [hara.object :as object]
            [kodai.core.pipeline :as pipe]
            [kodai.core.format :as format])
  (:import java.awt.event.KeyEvent))

(def +default-options+
  {:format {:label :initials :skip 1}
   :bundle {:reverse-calls     false  
            :hide-dynamic      false
            :hide-namespaces   #{}    
            :hide-singletons   false
            :hide-vars         #{}    
            :select-namespaces #{}    
            :select-vars       #{}    
            :collapse-vars     #{}}})

(def +default-style+
  [[:node {:size "8px"
           :text-mode "hidden"
           :fill-color "grey"}]
   [:node:clicked {:text-mode "normal"
                   :text-color "black"}]
   [:node.selected {:stroke-width "5px"
                    :stroke-color "black"
                    :stroke-mode "plain"}]
   [:node.focused_node {:fill-color "green"
                        :stroke-width "2px"
                        :stroke-color "black"
                        :text-mode "normal"
                        :stroke-mode "plain"
                        :size "25px"
                        :text-size "20px"
                        :text-color "black"
                        :z-index 100}]
   [:node.adjacent_node {:fill-color "blue"
                         :size "10px"
                         :text-size "10px"
                         :text-color "black"
                         :z-index 100}]
   [:node.downstream_node {:fill-color "red"}]
   [:edge.adjacent_edge {}]
   [:edge.downstream_edge {}]])


(object/extend-maplike
 
 gulfstream.core.Browser
 {:tag "browser"
  :default false
  :proxy   {:graph [:attributes :style :title]}
  :getters {:dom (fn [b] (-> b :dom deref))
            :options (fn [b] (-> b :options deref))
            :calls (fn [b] (-> b :calls deref))
            :interaction (fn [b] (-> b :interaction deref))
            :elements (fn [b] (-> b :elements deref))}
  :setters {:dom (fn [b dom] (reset! (:dom b) dom) b)
            :options (fn [b options] (reset! (:options b) options) b)
            :calls (fn [b calls] (reset! (:calls b) calls) b)
            :interaction (fn [b interaction] (reset! (:interaction b) interaction) b)
            :elements (fn [b elements] (reset! (:elements b) elements) b)}})

(def keyboard
  {KeyEvent/VK_ESCAPE  :escape
   KeyEvent/VK_ENTER   :enter
   KeyEvent/VK_SPACE   :space
   KeyEvent/VK_SHIFT   :shift
   KeyEvent/VK_CONTROL :control
   KeyEvent/VK_META    :meta
   KeyEvent/VK_ALT     :alt
   KeyEvent/VK_D       :d
   KeyEvent/VK_R       :r
   KeyEvent/VK_H       :h
   KeyEvent/VK_I       :i
   KeyEvent/VK_S       :s})

(defn create-listeners [interaction options]
  {:node
   {:on-push (fn [id]
               (swap! interaction assoc :focused (keyword id)))}
   :keyboard
   {:on-push (fn [e]
               (let [k (keyboard (.getKeyCode e))]
                 (cond (= k :escape) 
                       (swap! interaction assoc :focused nil)

                       (= k :r)
                       (swap! options update-in [:bundle :reverse-calls] not)
                      
                       (= k :i)
                       (swap! options update-in [:bundle :hide-singletons] not)
                       
                       (= k :d)
                       (swap! options update-in [:bundle :hide-dynamic] not)

                       (= k :s)
                       (if-let [focused (-> interaction deref :focused)]
                         (swap! options update-in [:bundle :select-vars]
                                #(if (% focused)
                                   (disj % focused)
                                   (conj % focused))))

                       (= k :h)
                       (if (get (:chords @interaction) :control)
                         (swap! options update-in [:bundle :hide-vars] (fnil empty #{}))
                         (when-let [focused (-> interaction deref :focused)]
                           (swap! interaction dissoc :focused)
                           (swap! options update-in [:bundle :hide-vars]
                                  #(if (% focused)
                                     (disj % focused)
                                     (conj % focused)))))
                       
                       (#{:shift :control :meta :alt} k)
                       (swap! interaction update-in [:chords] (fnil #(conj % k) #{})))))
    :on-release (fn [e]
                  (let [k (keyboard (.getKeyCode e))]
                    (cond (#{:shift :control :meta :alt} k)
                          (swap! interaction update-in [:chords] disj k))))}})

(defn add-class [dom type id cls]
  (update-in dom [type id :ui.class] (fnil #(conj % cls) [])))

(defn remove-class [dom type id cls]
  (update-in dom [type id]
             (fn [m]
               (let [arr (:ui.class m)
                     arr (if (vector? arr) arr [arr])
                     res (vec (remove #(= % cls) arr))]
                 (if (empty? res)
                   (dissoc m :ui.class)
                   (assoc m :ui.class res))))))

(defn add-focus [dom calls var]
  (let [dom (if var
              (-> dom
                  (add-class :nodes var "focused_node")
                  ;;(manipulate-dom calls pipe/find-adjacent add-class var   "adjacent-node")
                  ;;(manipulate-dom calls pipe/find-downstream add-class var "downstream-node")
                  )
              dom)
        ;;_ (println "ADDED:" (get-in dom :))
        ]
    dom))

(defn remove-focus [dom calls var]
  (let [dom (if var
              (-> dom
                  (remove-class :nodes var "focused_node")
                  ;;(manipulate-dom calls pipe/find-adjacent remove-class var   "adjacent-node")
                  ;;(manipulate-dom calls pipe/find-downstream remove-class var "downstream-node")
                  )
              dom)]
    dom))


(comment
  (defn manipulate-dom
    [dom calls select-fn change-fn id tag]
    (reduce (fn [dom id]
              (change-fn dom id tag))
            (select-fn calls id))))

(defn viewer
  ([app]
   (viewer app {}))
  ([{:keys [bundle] :as app} options]
   (let [options (nested/merge-nested +default-options+ options)
         options-cell (atom nil)
         calls-cell   (atom {})
         elements-cell   (atom {})
         interaction-cell (atom {})
         listeners    (create-listeners interaction-cell options-cell)
         viewer       (gs/browse (assoc app :dom {:nodes {} :edges {}} :style +default-style+ :listeners listeners))
         dom-cell     (get viewer :dom)]
     (latch/latch options-cell calls-cell
                  #(pipe/call-pipe bundle %))
     (latch/latch calls-cell elements-cell
                  #(pipe/elements-pipe % (deref options-cell)))
     (latch/latch elements-cell dom-cell
                  #(add-focus % (deref calls-cell) (-> interaction-cell deref :focused)))
     (watch/add interaction-cell :interaction
                (fn [_ _ prev curr]
                  (swap! dom-cell #(-> %
                                       (remove-focus (deref calls-cell) prev)
                                       (add-focus (deref calls-cell) curr))))
                {:select [:focused]
                 :diff true})
     (reset! options-cell options)
     (-> viewer
         (assoc :options options-cell
                :calls calls-cell
                :elements elements-cell
                :interaction interaction-cell)))))

(comment
  (require '[kodai.bundle :as bundle])
  (require '[kodai.core.pipeline :as pipe])

  (def bd (bundle/bundle #"src/kodai"))

  (count (pipe/call-pipe bd {:bundle {:hide-singletons true}}))
  (count (pipe/call-pipe bd {}))
  
  
  (gs/browse
   {:dom (-> (pipe/elements-pipe
              (pipe/call-pipe bd {:bundle {:reverse-calls false
                                           :hide-singletons true}})
              {:format {:label :initials}}))
    :style +default-style+
    })

  (clojure.set/intersection (pipe/find-singletons (:forward bd))
                            (pipe/find-singletons (:reverse bd)))
  
  
  
  (comment
    h   - hide var
    C-h - clear hidden vars
    H   - hide namespace
    C-H - clear hidden namespaces
    s - select/unselect var 
    S - select/unselect namespace
    c - collapse/uncollapse var
    d - toggle dynamic vars
    r - toggle call-reverse
    i - toggle singleton vars)
  
  
  
  (count (-> bd :reverse keys))

  (clojure.set/difference #{1 2} #{1 3})
  (def vw (viewer {:bundle bd} {:bundle {:hide-singletons true}}))
  
  (vw :interaction)
  (vw :options)
  
  (-> (vw :dom)
      :nodes
      :kodai.core.viewer/viewer)
  
  (:elements )
  
   (pipe/metas-pipe (pipe/call-pipe bd {:bundle {:reverse-calls false
                                                 :hide-namespaces #{"kodai.util"}
                                                 ;;:select-vars #{:kodai.util/keep-vars}
                                                 :hide-singletons true}})


                    {:format {:label :partial}
                     :bundle {:reverse-calls false
                              :hide-namespaces #{"kodai.util"}
                              ;;:select-vars #{:kodai.util/keep-vars}
                              :hide-singletons true}})
   
   
  

  
  (def bw (gs/browse {:dom {:nodes {:a {}
                                    :b {}}
                            :edges {[:a :b] {}}}
                      :title "Hello World"}))
  (get bw :dom)
  (keys bw)
  
  (:graph :dom :viewer)
  
  (bw :dom {:nodes {:a {:label "a"}
                    :b {:label "b"}}
            :edges {[:a :b] {:label "b->c"}}})

  (bw :dom {:nodes {}
            :edges {[:a :b] {:label "a->b"}}}))
