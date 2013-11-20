(ns pliant.webpoint.middleware
  (:use pliant.webpoint.common)
  (:require [ring.util [codec :as codec]
                       [response :as ring]]
            [pliant.webpoint.request :as request]
            [clojure.tools.logging :as logging]))

;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
;; ContextPath-aware resource provider middleware.
;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
(defn wrap-resource
  "Middleware for ring that first checks to see whether the request map matches a static
  resource. If it does, the resource is returned in a response map, otherwise
  the request map is passed onto the handler. The root-path argument will be
  added to the beginning of the resource path."
  [handler root-path]
  (fn [request]
    (if-not (= :get (:request-method request))
      (handler request)
      (let [uri-path (path request)
            path (.substring ^String (codec/url-decode uri-path) 1)]
        (or (ring/resource-response path {:root root-path})
            (handler request))))))


(defn inject-routes
  [& middlewares]
  (reduce #(%2 %1) request/route middlewares))

;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
;; Keyify parameters middleware.
;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
(defn keyify-request-params
  "Turns all the map keys to keywords for the request properties indicated."
  [request keys]
  (reduce #(if (%1 %2)
             (assoc %1 %2 (keyify-params (%1 %2)))
             %1) request keys))


(defn wrap-keyify-params
  "Middleware that will converst map keys to keywords."
  [handler & keys]
  (fn [request]
    (-> request
        (keyify-request-params keys)
        handler)))

;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
;; Log request middleware.
;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
(defn log-request-cleaner
  "Data cleaner that returns the data without omitting anything."
  [request params]
  params)

(defn log-response-cleaner
  "Data cleaner that returns the data without omitting anything."
  [response]
  response)

(defn log-request
  [request cleaner]
  (logging/debug "---------- ---------- ---------- ---------- ---------- ---------- ----------" )
  (logging/debug (str "REQUEST KEYS: " (keys request)))
  (logging/debug (str "METHOD:       " (:request-method request)))
  (logging/debug (str "URI:          " (:uri request)))
  (logging/debug (str "CONTENT-TYPE: " (:content-type request)))
  (logging/debug (str "CONTEXT:      " (:context request)))
  (logging/debug (str "PATH-INFO:    " (:path-info request)))
  (logging/debug (str "QUERY-PARAMS: " (cleaner request (:query-params request))))
  (logging/debug (str "PARAMS:       " (cleaner request (:params request))))
  (logging/debug (str "FORM-PARAMS:  " (cleaner request (:form-params request))))
  (logging/debug (str "BODY-PARAMS:  " (cleaner request (:body-params request))))
  request)

(defn log-response
  [response cleaner]
  (logging/debug "========== ========== ========== ========== ========== ========== ==========" )
  (logging/debug (str "RESPONSE:     " (cleaner response)))
  (logging/debug "---------- ---------- ---------- ---------- ---------- ---------- ----------" )
  response)

(defn wrap-log-request
  "Middleware that will log the attributes of the request."
  [handler & {:as opts}]
  (fn [request]
    (-> request
        (log-request (:request-cleaner opts log-request-cleaner))
        handler
        (log-response (:response-cleaner opts log-response-cleaner)))))


;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
;; Body resolution middleware.
;; ---------- ---------- ---------- ---------- ---------- ---------- ----------
(defn resolve-body
  "If available, resolves the body into a data structure and stores it as :body-params"
  [request]
  (assoc request :body-params (resolve-body-by-content-type request)))


(defn wrap-resolve-body
  "Middleware that will resolve the body of the request."
  [handler]
  (fn [request]
    (-> request
        resolve-body
        handler)))


