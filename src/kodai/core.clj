(ns kodai.core
  (:require [gulfstream.core :as gs]
            [hara.object :as object]
            [kodai.core.provision :as provision]
            [kodai.bundle :as bundle]
            [clojure.string :as string]
            [hara.data.nested :as nested]
            [hara.common.watch :as watch])
  (:import java.awt.event.KeyEvent))

(def +default-modifiers+
  {:style [[:node.selected {:fill-color "green"
                            :size "30px"}]]
   :label {:type :initials}
   :ui   {:selected nil
          :chords #{}}
   :data {:type :forward
          :select #{}
          :collapse #{}}})

(def keyboard
  {KeyEvent/VK_ESCAPE  :escape
   KeyEvent/VK_ENTER   :enter
   KeyEvent/VK_SPACE   :space
   KeyEvent/VK_SHIFT   :shift
   KeyEvent/VK_CONTROL :control
   KeyEvent/VK_META    :meta
   KeyEvent/VK_ALT     :alt})

(defn provision-listeners
  [modifiers]
  {:node    {:on-push (fn [id]
                        (println "VIEWER:" id)
                        (swap! modifiers update-in [:ui] assoc :selected (keyword id)))}
   :keyboard {:on-push (fn [e]
                         (let [k (keyboard (.getKeyCode e))]
                           (cond (= k :escape) 
                                 (swap! modifiers update-in [:ui] assoc :selected nil)
                                 
                                 (#{:shift :control :meta :alt} k)
                                 (swap! modifiers update-in [:ui :chords] conj k))))
              :on-release (fn [e]
                            (let [k (keyboard (.getKeyCode e))]
                              (cond (#{:shift :control :meta :alt} k)
                                    (swap! modifiers update-in [:ui :chords] disj k))))}})

(defn add-selection-watch [dom modifiers]
  (watch/add modifiers :selection
             (fn [_ _ p n]
               (println "WATCH:" p n)
               (swap! dom (fn [dom] (-> dom
                                        (update-in [p :ui.class] (fn [v] (vec (remove #(= % "selected") v))))
                                        (update-in [n :ui.class] (fn [v] (fnil #(conj % "selected") [])))))))
             {:select [:ui :selected]
              :diff true})
  dom)

(defn browse
  ([bundle] (browse bundle {}))
  ([bundle modifiers]
   (let [modifiers (nested/merge-nested +default-modifiers+ modifiers)
         dom       (provision/provision-dom bundle modifiers)
         style     (provision/provision-style modifiers)
         attrs     (provision/provision-attributes modifiers)
         mods      (atom modifiers)
         listeners (provision-listeners mods)
         _         (println "DOM:" dom)
         browser   (gs/browse {:dom   dom
                               :style style
                               :attributes attrs
                               :modifiers mods
                               :listeners listeners})]
     
     (-> (:dom browser)
         (add-selection-watch mods))
     browser)))

(comment
  (-> (bundle/bundle #"src/gulfstream")
      browse)

  
  )


