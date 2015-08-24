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
            :highlight-vars    #{}
            :select-namespaces #{}    
            :select-vars       #{} 
            :collapse-vars     #{}}})

(def +default-style+
  [[:node                 {:size "8px"
                           :text-mode "hidden"
                           :fill-color "grey"}]
   [:node:clicked         {:text-mode "normal"
                           :text-color "black"}]
   [:node.selected        {:stroke-width "5"
                           :stroke-color "black"
                           :stroke-mode "plain"}]
   [:node.collapsed       {:fill-color "orange"
                           :text-mode "normal"}]
   [:node.highlighted     {:text-mode "normal"}]
   [:node.focused_node    {:fill-color "green"
                           :text-mode "normal"
                           :size "25px"
                           :text-size "20px"
                           :text-color "black"
                           :z-index "100"}]
   [:node.adjacent_node   {:fill-color "blue"
                           :size "10px"
                           :text-size "12px"
                           :text-mode "normal"
                           :text-color "blue"
                           :z-index "100"}]
   [:node.downstream_node {:fill-color "red"}]
   [:edge.adjacent_edge   {:fill-color "blue"
                           :size "12px"}]
   [:edge                 {:fill-color "grey"}]
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
   KeyEvent/VK_C       :c
   KeyEvent/VK_D       :d
   KeyEvent/VK_H       :h
   KeyEvent/VK_I       :i
   KeyEvent/VK_L       :l
   KeyEvent/VK_N       :n
   KeyEvent/VK_R       :r
   KeyEvent/VK_S       :s
   KeyEvent/VK_V       :v})

(defn toggle-focused
  ([interaction options k]
   (toggle-focused interaction options k identity))
  ([interaction options k f]
   (if-let [focused (-> interaction deref :focused)]
     (swap! options update-in [:bundle k]
            #(if (% focused)
               (disj % focused)
               (conj (f %) focused))))))

(defn create-listeners [interaction options]
  {:node
   {:on-push (fn [id]
               (if (get (:chords @interaction) :meta)
                 (swap! interaction assoc :focused (keyword id))))}
   :keyboard
   {:on-push (fn [e]
               (let [k (keyboard (.getKeyCode e))]
                 (cond (= k :escape) 
                       (swap! interaction assoc :focused nil)

                       (= k :r)
                       (swap! options update-in [:bundle :reverse-calls] not)
                      
                       (= k :s)
                       (swap! options update-in [:bundle :hide-singletons] not)
                       
                       (= k :d)
                       (swap! options update-in [:bundle :hide-dynamic] not)

                       (= k :v)
                       (toggle-focused interaction options :select-vars empty)

                       (= k :c)
                       (toggle-focused interaction options :collapse-vars)                       

                       (= k :l)
                       (toggle-focused interaction options :highlight-vars)
                       
                       (= k :h)
                       (if (get (:chords @interaction) :control)
                         (swap! options update-in [:bundle :hide-vars] (fnil empty #{}))
                         (when-let [focused (-> interaction deref :focused)]
                           (toggle-focused interaction options :hide-vars)
                           (swap! interaction dissoc :focused)))
                       
                       (= k :n)
                       (if (get (:chords @interaction) :control)
                         (swap! options update-in [:bundle :hide-namespaces] (fnil empty #{}))
                         (when-let [focused (if-let [v (-> interaction deref :focused)]
                                              (.getNamespace v))]
                           (swap! interaction dissoc :focused)
                           (swap! options update-in [:bundle :hide-namespaces]
                                  #(if (% focused)
                                     (disj % focused)
                                     (conj % focused)))))
                       
                       (#{:shift :control :meta :alt} k)
                       (swap! interaction update-in [:chords] (fnil #(conj % k) #{})))))
    :on-release (fn [e]
                  (let [k (keyboard (.getKeyCode e))]
                    (cond (#{:shift :control :meta :alt} k)
                          (swap! interaction update-in [:chords] disj k))))}})

(defn add-class
  "adds a :ui.class to a dom
   (add-class {:nodes {:a {}}}
              :nodes :a \"hello\")
   => {:nodes {:a {:ui.class [\"hello\"]}}}"
  {:added "0.1"}
  [dom type id cls]
  (update-in dom [type id :ui.class] (fnil #(conj % cls) [])))

(defn remove-class
  "adds a :ui.class to a dom
   (remove-class {:nodes {:a {:ui.class [\"hello\" \"world\"]}}}
                 :nodes :a \"hello\")
   => {:nodes {:a {:ui.class [\"world\"]}}}"
  {:added "0.1"}
  [dom type id cls]
  (update-in dom [type id]
             (fn [m]
               (let [arr (:ui.class m)
                     arr (if (vector? arr) arr [arr])
                     res (vec (remove #(= % cls) arr))]
                 (if (empty? res)
                   (dissoc m :ui.class)
                   (assoc m :ui.class res))))))

(defn manipulate
  [dom calls select-fn change-fn type id tag]
  (reduce (fn [dom id]
            (change-fn dom type id tag))
          dom
          (select-fn calls id)))

(defn add-focus [dom calls var]
  (let [dom (if var
              (-> dom
                  (add-class :nodes var "focused_node")
                  (manipulate calls pipe/find-adjacent add-class :nodes var  "adjacent_node")
                  (manipulate calls pipe/find-downstream add-class :nodes var "downstream_node")
                  )
              dom)]
    dom))

(defn remove-focus [dom calls var]
  (let [dom (if var
              (-> dom
                  (remove-class :nodes var "focused_node")
                  (manipulate calls pipe/find-adjacent remove-class :nodes var "adjacent_node")
                  (manipulate calls pipe/find-downstream remove-class :nodes var "downstream_node"))
              dom)]
    dom))

(defn viewer
  "creates a viewer for the bundle"
  {:added "0.1"}
  ([app]
   (viewer app {}))
  ([{:keys [bundle] :as app} options]
   (let [options (nested/merge-nested +default-options+ options)
         options-cell (atom nil)
         calls-cell   (atom {})
         elements-cell   (atom {})
         interaction-cell (atom {})
         listeners    (create-listeners interaction-cell options-cell)
         viewer       (gs/browse (assoc app
                                        :dom {:nodes {} :edges {}}
                                        :style +default-style+
                                        :listeners listeners))
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
