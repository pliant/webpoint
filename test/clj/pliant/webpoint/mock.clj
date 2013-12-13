(ns pliant.webpoint.mock
  (:use [clojure.string :only (join)]))

(defn mock-request 
  ([] {:body nil
       :body-params {}
       :character-encoding nil
       :content-length nil
       :content-type nil
       :cookies nil
       :flash nil
       :form-params {}
       :headers {}
       :params {}
       :query-params {}
       :query-string nil
       :remote-addr nil
       :request-method :get
       :scheme nil
       :server-name "localhost"
       :server-port 8088
       :session nil
       :session/key nil
       :ssl-client-cert nil
       :uri nil})
  ([& opts]
    (cond
      (and (= 1 (count opts)) (map? (first opts))) (merge (mock-request) (first opts))
      (even? (count opts)) (apply assoc (mock-request) opts)
      :else
        (mock-request))))


(defn +accept
  [request & types]
  (assoc-in request [:headers "accept"] (join "," (map name types))))
