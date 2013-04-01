(ns pliant.webpoint.middleware
  (:use pliant.webpoint.common)
  (:require [compojure.handler :as handler]
            [ring.middleware.session :as session]
            [ring.util [codec :as codec]
                       [response :as ring]]
            [pliant.webpoint.request :as request]))


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
  (-> (reduce #(%2 %1) request/routes middlewares)
      session/wrap-session
      handler/api))
