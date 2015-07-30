(defproject helpshift/kodai "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [venantius/yagni "0.1.2"]
                 [im.chit/jai "0.2.5"]]
  :source-paths ["src" "example"]
  :profiles {:dev {:dependencies [[garden "1.2.5"]
                                  [net.sourceforge.cssparser/cssparser "0.9.16"]
                                  [org.graphstream/gs-ui "1.3"]]}})
