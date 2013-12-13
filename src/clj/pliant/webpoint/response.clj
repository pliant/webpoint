(ns pliant.webpoint.response
  (:use pliant.webpoint.common
        [pliant.process :only [defprocess deflayer continue]]
        [clojure.data.json :only [write-str]])
  (:require [ring.util.response :as ring]
            [clojure.tools.logging :as logging]))


(defn to-json-response
  "Converts a data structure to a JSON response structure, with the data converted to a JSON string."
  [body]
  (-> body
    write-str
    ring/response
    (ring/content-type mime-json)))


(defn to-clojure-response
  "Converts a data structure to a Clojure response structure, with the data serialized to a Clojure string."
  [body]
  (-> body
    prn-str
    ring/response
    (ring/content-type mime-clojure)))


(defprocess respond-with-data
  "The reponse process that returns some serialized data structure.  The typical formats are 
   JSON(default), Clojure, XML."
  [body request]
  (to-json-response body))

(deflayer respond-with-data respond-with-clojure
  "A reponse layer that will return a JSON response if requested."
  [body request]
  (if (expects? request mime-clojure mime-clojure-text)
    (to-clojure-response body)
    (continue)))


(defprocess respond-with-html
  "The reponse process that returns text to be interpretted as HTML.  By default, it expects the body to be 
   html formated."
  [body request]
  (-> body
    ring/response
    (ring/content-type mime-html)))


(defprocess respond-with-default
  "The reponse process that is used when the return type is not detectable.  By default, the body is returned 
   as text."
  [body request]
  (if (string? body)
    (respond-with-html body request)
    (respond-with-data body request)))


(defprocess respond-type
  "Determines the response type that is requested. By default, will return the :default keyword response type."
  [response request]
  :default)

(deflayer respond-type ring-respond-type
  "Will check if the response is already a formatted ring response that should be handled by ring only. 
   This should always be first."
  [response request]
  (if (ring/response? response) 
    :response
    (continue)))

(deflayer respond-type data-respond-type
  "Will check if the expected response type is an data response type, such as JSON, Clojure, or XML.
   Will return :data if true."
  [response request]
  (if (expects-data? request)
    :data
    (continue)))

(deflayer respond-type html-respond-type
  "Will check if the expected response type is an HTML response type.  Will return :html if true."
  [response request]
  (if (expects? request mime-html)
    :html
    (continue)))


(defmulti respond 
  "Provides a point for all responses to be channeled through to be handled appropriately."
  respond-type)

(defmethod respond :response
  [response request]
  response)

(defmethod respond :data
  [response request]
  (respond-with-data response request))

(defmethod respond :html
  [response request]
  (respond-with-html response request))

(defmethod respond :default
  [response request]
  (respond-with-default response request))


(defn respond-in-error
  "Responds to the request with a status that is provided rather than the default '200' status code."
  [response request status]
  (ring/status (respond response request) status))


(defprocess on-exception
  "on-exception is called to handle any exception that is thrown which interrupts the normal processing of a request.
  By default, the exception is logged, and an error message is returned with an HTTP status of 401."
  [exception request]
  (logging/error exception (select-keys request [:request-method :uri :context :path-info :query-params :body-params]))
  (respond-in-error 
    request 
    (str "The process failed to execute successfully due to an unexpected exception.  "
         "The exception has been logged. Contact your administrator and inform them of the incident.")
    401))
