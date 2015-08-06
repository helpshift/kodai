(ns kodai.visualise.entry-modal
  (:require [seesaw.core :as ui]))

(comment
  (ui/native!)

  (def f (ui/frame))
  (ui/config! f :content (ui/label "Hello"))
  (-> f ui/pack! ui/show!)
  ) 
