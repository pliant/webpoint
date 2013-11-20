(ns pliant.webpoint.common
  (:use [clojure.data.json :only (read-json)]
        [clojure.string :only (blank? join split)]))

(def mime-html "text/html")
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

(defn form-request?
  [^String content-type]
  (.startsWith content-type mime-urlencoded))

(defn resolve-body-by-content-type
  "If the body is available to be resolved, it will be read. There are instances when
   the stream to the body has been closed and should not be read, such as when an HTML
   form submits data."
  [request]
  (let [content-type (or (:content-type request) "NA")
        data (:form-params request)
        body (:body request)]
    (cond
     (json-requested? content-type) (get-json-params body)
     (clojure-requested? content-type) (read-clojure body)
     (form-request? content-type) (keyify-params data)
     (< 0 (count data)) (keyify-params data)
     :else {})))
