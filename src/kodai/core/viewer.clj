(ns kodai.core.viewer
  (:require [gulfstream.core :as gs]
            [hara.concurrent.latch :as latch]
            [kodai.core.pipeline :as pipe])
  (:import java.awt.event.KeyEvent))

(defonce +default-options+
  {:reverse-calls     false  
   :hide-dynamic      true   
   :hide-namespaces   #{}    
   :hide-singletons   true   
   :hide-vars         #{}    
   :select-namespaces #{}    
   :select-vars       #{}    
   :collapse-vars     #{}})

(def keyboard
  {KeyEvent/VK_ESCAPE  :escape
   KeyEvent/VK_ENTER   :enter
   KeyEvent/VK_SPACE   :space
   KeyEvent/VK_SHIFT   :shift
   KeyEvent/VK_CONTROL :control
   KeyEvent/VK_META    :meta
   KeyEvent/VK_ALT     :alt})

(defn create-listeners [interaction]
  {:viewer
   {:on-push (fn [id]
               (swap! interaction update-in [:ui] assoc :selected (keyword id)))}
   :keys
   {:on-push (fn [e]
               (let [k (keyboard (.getKeyCode e))]
                 (cond (= k :escape) 
                       (swap! interaction update-in [:ui] assoc :selected nil)
                       
                       (#{:shift :control :meta :alt} k)
                       (swap! interaction update-in [:ui :chords] conj k))))
    :on-release (fn [e]
                  (let [k (keyboard (.getKeyCode e))]
                    (cond (#{:shift :control :meta :alt} k)
                          (swap! interaction update-in [:ui :chords] disj k))))}})

(defn add-interaction [dom interaction]
  dom)

(defn remove-interaction [dom interaction]
  dom)

(defn update-interaction [dom previous current]
  (-> dom
      (remove-interaction previous)
      (add-interaction current)))

(defn create-dom [calls metas interaction]
  {:nodes {}
   :edges {}})

(defn viewer
  ([app]
   (viewer +default-options+))
  ([{:keys [bundle] :as app} options]
   (let [options-cell (atom {})
         calls-cell   (atom {})
         metas-cell   (atom {})
         interaction-cell (atom {})
         listeners    (create-listeners interaction-cell)
         viewer       (gs/browse (assoc app :dom {} :listeners listeners))
         dom-cell     (get bw :dom)]
     (latch/latch options-cell calls-cell
                  #(pipe/call-pipe bundle %))
     (latch/latch calls-cell metas-cell
                  #(pipe/meta-pipe % (deref options-cell)))
     (latch/latch metas-cell dom-cell
                  #(-> (create-dom (deref calls-cell) %)
                       (add-interaction (deref interaction-cell))))
     (add-watch interaction-cell :interaction
                (fn [_ _ prev curr]
                  (swap! dom-cell update-interaction prev curr)))
     (reset! options-cell options)
     (-> viewer
         (assoc :options options-cell
                :calls calls-cell
                :metas metas-cell
                :interaction interaction-cell)))))

(comment
  (require '[kodai.bundle :as bundle])
  (require '[kodai.core.pipeline :as pipe])

  (def bd (bundle/bundle #"src/kodai"))

  (-> bd :reverse :kodai.util/keep-vars)

  
  
  (pipe/call-pipe bd {:reverse-calls true
                      ;;:hide-namespaces #{"kodai.util"}
                      ;;:select-vars #{:kodai.util/keep-vars}
                      :hide-singletons true})

  
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
            :edges {[:a :b] {:label "a->b"}}})
  
  )
