(ns kodai.core
  (:require [gulfstream.core :as gs]
            [gulfstream.graph :as graph]
            [hara.object :as object]
            [kodai.core.provision :as provision]
            [kodai.bundle :as bundle]
            [clojure.string :as string]
            [hara.data.nested :as nested]
            [hara.common.watch :as watch])
  (:import java.awt.event.KeyEvent))

(def +default-modifiers+
  {:style [[:node {:size "8px"
                   :text-mode "hidden"
                   :text-color "grey"
                   :fill-color "grey"}]
           [:node.selected {:fill-color "green"
                            :stroke-width "2px"
                            :stroke-color "black"
                            :text-mode "normal"
                            :stroke-mode "plain"
                            :size "25px"
                            :text-size "20px"
                            :text-color "black"
                            :z-index 100}]
           [:edge {:fill-color "grey"
                   :arrow-size "3px, 3px"}]]
   :label {:type :partial}
   :ui    {:selected nil
           :hide-text true
           :chords #{}}
   :data  {:type :forward
           :dynamic :show
           :singletons :show
           :namespaces #{}
           :entries #{}
           :hide #{}
           :collapse #{}}})

(comment
  (-> (bundle/bundle #"src/gulfstream")
      browse)

  (object/access gs/+current+ :style
                 (:style +default-modifiers+))

  bundle
  (-> entries)



  )

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

(defn change-selection [dom prev curr]
  (let [dom (if prev
              (update-in dom [prev]
                         (fn [m]
                           (let [cls (:ui.class m)
                                 res (vec (remove #(= % "selected") cls))]
                             (if (empty? res)
                               (dissoc m :ui.class)
                               (assoc m :ui.class res)))))
              dom)
        dom (if curr
              (update-in dom [curr :ui.class] (fnil #(conj % "selected") []))
              dom)]
    dom))

(defn add-selection-watch [dom modifiers]
  (watch/add modifiers :selection
             (fn [_ _ prev curr]
               (swap! dom change-selection prev curr))
             {:select [:ui :selected]
              :diff true})
  dom)

(defn add-call-type-watch [dom elements bundle modifiers]
  (watch/add modifiers :call-type
             (fn [_ _ p n]
               (reset! elements (provision/provision-elements bundle @modifiers)))
             {:select [:data :type]
              :diff true})
  dom)

(defn add-elements-watch [dom elements modifiers]
  (watch/add elements :elements
             (fn [_ _ _ curr]
               (reset! dom (provision/provision-dom curr @modifiers))))
  dom)

(defn hook-up [bundle elements dom style attrs modifiers]
  (let [modifiers (atom modifiers)
        elements  (atom elements)
        listeners (provision-listeners modifiers)
        browser   (gs/browse {:dom   dom
                              :style style
                              :attributes attrs
                              :modifiers modifiers
                              :listeners listeners
                              :bundle bundle})]
    (-> (:dom browser)
         (add-elements-watch elements modifiers)
         (add-call-type-watch elements bundle modifiers)
         (add-selection-watch modifiers))
    browser))

(defn browse
  ([bundle] (browse bundle {}))
  ([bundle modifiers]
   (let [modifiers (nested/merge-nested +default-modifiers+ modifiers)
         elements  (provision/provision-elements bundle modifiers)
         dom       (provision/provision-dom elements modifiers)
         style     (provision/provision-style modifiers)
         attrs     (provision/provision-attributes modifiers)]
     (hook-up bundle elements dom style attrs modifiers))))


(comment


  (swap! (:dom gs/+current+) update-in [:gulfstream.graph.css/property-pair :ui.class] (fnil #(conj % "selected") []))
  (:bundle gs/+current+)

  (get-in @(:dom gs/+current+) [:gulfstream.graph.css/emit :ui.class])

  (swap! (:modifiers gs/+current+) update-in [:data :type] (constantly :reverse))
  (swap! (:modifiers gs/+current+) update-in [:data :type] (constantly :forward))


  (get-in @(:dom gs/+current+) [:gulfstream.core/->Browser])

  (get-in @(:dom gs/+current+) [:gulfstream.core/->Browser])

  (get-in (object/access (:graph gs/+current+) :dom)
          [:gulfstream.core/->Browser])

  (.removeAttribute (graph/element (:graph gs/+current+) :gulfstream.core/->Browser)
                    "ui.class")


  (swap! (:dom gs/+current+) update-in [:gulfstream.core/->Browser :ui.class]
         (fn [v] (vec (remove #(= % "selected") v))))

  (swap! (:dom gs/+current+) update-in [:gulfstream.core/->Browser :ui.class]
         (fnil #(conj % "selected") []))

  (swap! (:dom gs/+current+) update-in [:gulfstream.core/->Browser :ui.class]
         (fnil #(conj % "other") []))


  )
