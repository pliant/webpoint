(ns pliant.webpoint.response
  (:use pliant.webpoint.common
        [clojure.data.json :only (pprint-json)])
  (:require [ring.util.response :as ring]
            [clojure.tools.logging :as logging]))

(defn respond-with-json
  [body request]
  (if (ring/response? body)
    (ring/content-type (update-in body [:body] (fn [b] (with-out-str (pprint-json b)))) mime-json)
    (ring/content-type (ring/response (with-out-str (pprint-json body))) mime-json)))

(defn respond-with-clojure
  [body request]
  (if (ring/response? body)
    (ring/content-type (update-in body [:body] (fn [b] (with-out-str (prn b)))) mime-clojure)
    (ring/content-type (ring/response (with-out-str (prn body))) mime-clojure)))

(defn format-response-body
  [body request]
  (let [requested-content-type (or (:content-type request) "NA")]
    (cond
     (and (ring/response? body) (not (empty? (:headers body))))
       body
     (json-requested? requested-content-type)
       (if (ring/response? body)
         (ring/content-type (update-in body [:body] (fn [b] (with-out-str (pprint-json b)))) requested-content-type)
         (ring/content-type (ring/response (with-out-str (pprint-json body))) requested-content-type))
     (or (map? body) (clojure-requested? requested-content-type))
       (if (ring/response? body)
         (ring/content-type (update-in body [:body] (fn [b] (with-out-str (prn b)))) mime-clojure)
         (ring/content-type (ring/response (with-out-str (prn body))) mime-clojure))
     :else body)))

(defmulti respond
  (fn [request body] 
    (let [contype (or (:content-type request) "NA")]
      (cond 
        (and (ring/response? body) (not (empty? (:headers body))))
          :response
        (json-requested? contype) 
          :json
        (clojure-requested? contype) 
          :clojure
        :else 
          :default))))

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
  body)


(defmulti on-exception 
  (fn [exception request] (class exception)))

(defmethod on-exception :default
  [exception request]
  (logging/error exception (select-keys request [:request-method :uri :context :path-info :query-params :body-params]))
  (respond {:status 401 
            :body {:message "The process failed to execute successfully due to an unexpected exception.  
                      The exception has been logged. Contact your administrator and inform them of the incident."}}
           request))
