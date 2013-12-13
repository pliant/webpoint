(defproject pliant/webpoint "0.1.1"
  :description "Provides a library that simplifies handling of web requests by matching the method and URL against a multimethod value."
  
  :url "https://github.com/pliant/webpoint"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  
  ;; Keep java source and project definition out of the artifact
  :jar-exclusions [#"^\." #"^*\/\." #"\.java$" #"project\.clj"]
  
  :signing {:gpg-key "B42493D5"}

  :dependencies [[pliant/process "0.1.1"]
                 [org.clojure/clojure "1.4.0"]
                 [ring/ring-core "1.2.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [org.clojure/data.json "0.2.3"]]
  
  :profiles {:provided {:dependencies [[javax.servlet/servlet-api "2.5"]]} })
