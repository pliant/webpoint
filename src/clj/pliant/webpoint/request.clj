(ns pliant.webpoint.request
  "Provides the routing of requests to funtional endpoints.  This is a very simplified way 
   for handling requests by mapping each method-url to a function, passing the entire 
   request to that function."
  (:use pliant.webpoint.common)
  (:require [pliant.webpoint.response :as response]
            [ring.util.response :as ring]))


(defmulti endpoints request->process-name)

(defmethod endpoints :default
  [request]
  (if (expects-data? request)
    (ring/status 
      (response/respond-with-data 
        {:message (str "Unable to find a process registered as '" (request->process-name request) "'.")} request) 404)
    (ring/not-found 
      (str "<html><head><title>Process Not Found</title></head><body>
            <h3>Unable to find a process registered as " (request->process-name request) "'.</h3></body></html>"))))

(defn route 
  "Routes a request to a functional endpoint."
  [request]
  (try
    (-> request
      endpoints
      (response/respond request))
    (catch Exception e
      (response/on-exception e request))))
