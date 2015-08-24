(defproject helpshift/kodai "0.1.0"
  :description "visualisations of function connectivity"
  :url "https://github.com/helpshift/kodai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [w01fe/sniper "0.1.0"]
                 [seesaw "1.4.5"]
                 [helpshift/gulfstream "0.1.8"]
                 [im.chit/hara.concurrent.latch "2.2.7"]]
  :source-paths ["src" "example"]
  :profiles {:dev {:dependencies [[midje "1.7.0"]
                                  [helpshift/hydrox "0.1.2"]]
                   :plugins [[lein-midje "3.1.3"]]}})
