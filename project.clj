(defproject pliant/webpoint "0.1.0-SNAPSHOT"
  :description "Provides a library that simplifies handling of web requests by matching the method and URL against a multimethod value."
  
  :url "https://github.com/pliant/webpoint"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :source-paths ["src/clj"]
  ;;:java-source-paths ["src/java"]
  :test-paths ["test/clj"]
  
  ;; Keep java source and project definition out of the artifact
  :jar-exclusions [#"^\." #"^*\/\." #"\.java$" #"project\.clj"]

  :dependencies [[org.clojure/clojure "1.3.0"]
                 [compojure "1.1.5"]
                 [ring/ring-core "1.1.7"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/data.json "0.1.2"]])
