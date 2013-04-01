(ns pliant.webpoint.common
  (:use [clojure.data.json :only (read-json)]
        [clojure.string :only (blank? join split)]))

(def mime-json "application/json")
(def mime-clojure "application/clojure")
(def mime-clojure-text "text/clojure")
(def mime-urlencoded "application/x-www-form-urlencoded")

(defn path
  [request]
  (or (:path-info request) (:uri request)))

(defn uri->process-name
  [method uri]
  (let [split-uri (filter #(not (blank? %)) (split uri #"\/"))]
    (join "-" (conj split-uri method))))

(defn get-json-params
  [body]
  (let [body-str (slurp body)]
    (if (not (blank? body-str))
      (read-json body-str))))

(defn read-clojure
  [body]
  (let [body-str (slurp body)]
    (if (not (blank? body-str))
      (binding [*read-eval* false]
        (read-string body-str)))))

(defn keyify-params
  [params]
  (if (map? params)
    (into {}
          (for [[k v] params]
            [(keyword k) v]))))

(defn json-requested?
  [^String content-type]
  (.startsWith ^String content-type mime-json))

(defn clojure-requested?
  [^String content-type]
  (or (.startsWith content-type mime-clojure-text)
      (.startsWith content-type mime-clojure)))
