(defproject helpshift/kodai "0.1.0"
  :description "statistical analysis for your code"
  :url "https://github.com/helpshift/kodai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [w01fe/sniper "0.1.0"]]
  :source-paths ["src" "example"]
  :profiles {:dev {:dependencies [[midje "1.7.0"]
                                  [garden "1.2.5"]
                                  [seesaw "1.4.5"]
                                  [net.sourceforge.cssparser/cssparser "0.9.16"]
                                  [org.graphstream/gs-ui "1.3"]]
                   :plugins [[lein-midje "3.1.3"]]}})
