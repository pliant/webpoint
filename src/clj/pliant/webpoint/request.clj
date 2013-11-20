(ns pliant.webpoint.request
  "Provides the routing of requests to funtional endpoints.  This is a very simplified way 
   for handling requests by mapping each method-url to a function, passing the entire 
   request to that function."
  (:use pliant.webpoint.common)
  (:require [pliant.webpoint.response :as response]
            [ring.util.response :as ring]))


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
        (ring/content-type 
          (ring/not-found (str "<html><head><title>Process Not Found</title></head><body><h1>Process Not Found</h1><h3>" 
                                  message "</h3></body></html>")) mime-html))))

(defn route 
  "Routes a request to a functional endpoint."
  [request]
  (try 
    (response/respond request (endpoints request))
    (catch Exception e
      (response/on-exception e request))))
