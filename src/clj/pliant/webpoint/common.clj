(ns pliant.webpoint.common
  (:use [clojure.data.json :only (read-json)]
        [clojure.string :only (blank? join split)]))

;; Mime Types To handle By Default
(def mime-text "text/text")
(def mime-html "text/html")
(def mime-json "application/json")
(def mime-clojure "application/clojure")
(def mime-clojure-text "text/clojure")
(def mime-urlencoded "application/x-www-form-urlencoded")

(def mimes-data (ref [mime-clojure mime-clojure-text mime-json]))

(defn add-data-mimes
  "Adds mime types to the mime-data collection that is used to determine if the 
   expected request is a data request."
  [& mimes]
  (when (seq mimes)
    (dosync
      (ref-set mimes-data (apply conj @mimes-data mimes)))))

(defn accept
  "Gets the mime-types that the requestor accepts.  Returns the supplied default value 
   or nill when no accept value is specified."
  ([request] (accept request nil))
  ([request default]
    (if-let [types (get-in request [:headers "accept"])]
      (map #(first (split % #"\;")) (split types #","))
      default)))

(defn content-type
  [request]
  (if-let [ct (:content-type request)]
    (first (split ct #"\;"))))

(defn expects?
  "Checks if the request will accept any of the specified mime types as a response."
  [request & mimes]
  (let [types (set (conj (accept request) (content-type request)))]
    (true? (some #(contains? types %) mimes))))

(defn expects-data?
  "Checks if the request expects any registered data types."
  [request]
  (apply expects? request @mimes-data))

(defn drop-leading-slash
  "Drops any leading '/' characters on the provided path string."
  [path]
  (apply str (drop-while (partial = \/) (seq path))))

(defn path
  [request]
  (or (:path-info request) (:uri request)))

(defn process-name
  [method uri]
  (let [split-uri (filter #(not (blank? %)) (split (or uri "") #"\/"))]
    (join "-" (conj split-uri method))))

(defn request->process-name
  [{:keys [request-method] :as request}]
    (process-name (name request-method) (path request)))


(defn body->json
  [body]
  (let [body-str (slurp body)]
    (if (not (blank? body-str))
      (read-json body-str))))

(defn body->clojure
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


(defmulti resolve-body-by-content-type content-type)

(defmethod resolve-body-by-content-type mime-json
  [{:keys [body] :as request}]
  (when body (body->json body)))

(defmethod resolve-body-by-content-type mime-clojure
  [{:keys [body] :as request}]
  (when body (body->clojure body)))

(defmethod resolve-body-by-content-type mime-clojure-text
  [{:keys [body] :as request}]
  (when body (body->clojure body)))

(defmethod resolve-body-by-content-type mime-urlencoded
  [{:keys [form-params] :as request}]
  (when form-params (keyify-params form-params)))

(defmethod resolve-body-by-content-type :default
  [_]
  {})
