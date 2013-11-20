(ns pliant.webpoint.response
  (:use pliant.webpoint.common
        [pliant.process :only (defprocess)]
        [clojure.data.json :only (write-str)])
  (:require [ring.util.response :as ring]
            [clojure.tools.logging :as logging]))

(defn respond-with-html
  [body request]
  (if (ring/response? body)
    (ring/content-type body mime-html)
    (ring/content-type (ring/response body) mime-html)))

(defn respond-with-json
  [body request]
  (if (ring/response? body)
    (ring/content-type (update-in body [:body] (fn [b] (write-str b))) mime-json)
    (ring/content-type (ring/response (write-str body)) mime-json)))

(defn respond-with-clojure
  [body request]
  (if (ring/response? body)
    (ring/content-type (update-in body [:body] (fn [b] (with-out-str (prn b)))) mime-clojure)
    (ring/content-type (ring/response (with-out-str (prn body))) mime-clojure)))


(defn respond-type
  [request body] 
  (let [contype (or (:content-type request) "NA")]
    (cond 
      (and (ring/response? body) (not (empty? (:headers body))))
        :response
      (json-requested? contype) 
        :json
      (clojure-requested? contype) 
        :clojure
      :else 
        :default)))

(defmulti respond respond-type)

(defmethod respond :response
  [request body]
  body)

(defmethod respond :json
  [request body]
  (respond-with-json body request))

(defmethod respond :clojure
  [request body]
  (respond-with-clojure body request))

(defmethod respond :default
  [request body]
  (ring/response body))


(defmulti respond-in-error (fn [request status body] (respond-type request body)))

(defmethod respond-in-error :response
  [request status body]
  (ring/status body status))

(defmethod respond-in-error :json
  [request status body]
  (ring/status (respond-with-json body request) status))

(defmethod respond-in-error :clojure
  [request status body]
  (ring/status (respond-with-clojure body request) status))

(defmethod respond-in-error :default
  [request status body]
  (ring/status (ring/response body) status))


(defprocess on-exception
  "on-exception is called to handle any exception that is thrown which interrupts the normal processing of a request.
  By default, the exception is logged, and an error message is returned with an HTTP status of 401."
  [exception request]
  (logging/error exception (select-keys request [:request-method :uri :context :path-info :query-params :body-params]))
  (respond-in-error request 401 
           {:message "The process failed to execute successfully due to an unexpected exception.  
                      The exception has been logged. Contact your administrator and inform them of the incident."}))
