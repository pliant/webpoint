(ns pliant.webpoint.request
  (:use pliant.webpoint.common
        [compojure.core :only (defroutes ANY)])
  (:require [pliant.webpoint.response :as response]
            [clojure.tools.logging :as logging]
            [ring.util.response :as ring]))

;; The resolve-body function has used cond to allow for more resolutions to be added.
;; For example, to add a resolution for handling multipart requests with files.
(defn resolve-body
  "If the body is available to be resolved, it will be read. There are instances when
   the stream to the body has been closed and should not be read, such as when an HTML
   form submits data."
  [request]
  (let [^String content-type (or (:content-type request) "NA")
        data (:form-params request)
        body (:body request)]
    (cond
     (.startsWith content-type mime-json) (get-json-params body)
     (.startsWith content-type mime-clojure) (read-clojure body)
     (.startsWith content-type mime-urlencoded) (keyify-params data)
     (< 0 (count data)) (keyify-params data)
     :else {})))

(defn clean-request-body-params
  [request]
  (assoc request :body-params (resolve-body request)))

(defn clean-request-query-params
  [request]
  (assoc request :query-params (keyify-params (:query-params request))))

(defn clean-request
  [request]
  (-> request
    clean-request-query-params
    clean-request-body-params))

(defmulti endpoints 
  (fn [{:keys [request-method] :as request}] 
    (uri->process-name (name request-method) (path request))))

(defmethod endpoints :default
  [{:keys [request-method] :as request}]
  (let [contype (or (:content-type request) "")
        proc-name (uri->process-name (name request-method) (path request))
        message (str "Unable to find a process registered as '" proc-name "'.")]
    (cond
      (or (json-requested? contype) (clojure-requested? contype))
        (ring/not-found {:message message})
      :else
        (ring/not-found (str "<html><head><title>Process Not Found</title></head><body><h1>Process Not Found</h1><h3>" 
                                message "</h3></body></html>")))))

(defn log-request
  [request]
  (logging/debug (str "METHOD: " (:request-method request)))
  (logging/debug (str "URI: " (:uri request)))
  (logging/debug (str "CONTEXT: " (:context request)))
  (logging/debug (str "PATH: " (:path-info request)))
  (logging/debug (str "QUERY: " (:query-params request)))
  (if (= "/security/login" (:path-info request))
    (logging/debug (str "BODY: " (dissoc (:body-params request) :password :new-password)))
    (logging/debug (str "BODY: " (:body-params request))))
  (logging/debug "--------------------" ))

(defroutes routes
  (ANY "*" request
       (let [clean-request (clean-request request)]
         (log-request clean-request)
         (try 
           (response/respond request (endpoints clean-request))
           (catch Exception e
             (response/on-exception e clean-request))))))
