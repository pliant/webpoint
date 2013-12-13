(ns pliant.webpoint.common-test
  (:use [clojure.test]
        [pliant.webpoint.common]
        [pliant.webpoint.mock]))

(deftest test-add-data-mimes
  (let [req (+accept (mock-request :content-type "blah") "erp")]
    (is (not (expects-data? req)))
    (add-data-mimes "blah")
    (is (expects-data? req))))
    

(deftest test-accepts
  (let [req (+accept (mock-request :content-type mime-text) mime-json mime-clojure)
        acc (accept req)]
    (is (some (partial = mime-json) acc))
    (is (some (partial = mime-clojure) acc))
    (is (not-any? (partial = mime-clojure-text) acc))
    (is (not-any? (partial = mime-text) acc))))


(deftest test-content-type
  (is (= mime-json (content-type (mock-request :content-type mime-json))))
  (is (= mime-json (content-type (mock-request :content-type (str mime-json "; charset-UTF-8"))))))


(deftest test-expects?
  (let [req (+accept (mock-request :content-type mime-text) mime-json mime-clojure)]
    (is (expects? req  mime-json))
    (is (expects? req  mime-clojure))
    (is (expects? req  mime-text))
    (is (not (expects? req  mime-clojure-text)))))


(deftest test-expects-with-meta?
  (let [req (+accept (mock-request :content-type (str mime-text "; charset=UTF-8")) 
                     mime-json (str mime-clojure "; charset=UTF-8"))]
    (is (expects? req  mime-json))
    (is (expects? req  mime-clojure))
    (is (expects? req  mime-text))
    (is (not (expects? req  mime-clojure-text)))))


(deftest test-expects-data?
  (let [req (+accept (mock-request :content-type mime-text) mime-json mime-clojure)]
    (is (not (expects-data? (mock-request :content-type mime-text))))
    (is (expects-data? (mock-request :content-type mime-json)))
    (is (expects-data? (mock-request :content-type mime-clojure)))
    (is (expects-data? (mock-request :content-type mime-clojure-text)))
    (is (expects-data? req))))


(deftest test-drop-leading-slash
  (is (= "path" (drop-leading-slash "path")))
  (is (= "path" (drop-leading-slash "/path")))
  (is (= "path" (drop-leading-slash "//path")))
  (is (= "path/" (drop-leading-slash "path/")))
  (is (= "path/" (drop-leading-slash "/path/")))
  (is (= "path/" (drop-leading-slash "//path/")))
  (is (= "path/htap" (drop-leading-slash "path/htap")))
  (is (= "path/htap" (drop-leading-slash "/path/htap")))
  (is (= "path/htap" (drop-leading-slash "//path/htap")))
  (is (= "path/htap/" (drop-leading-slash "path/htap/")))
  (is (= "path/htap/" (drop-leading-slash "/path/htap/")))
  (is (= "path/htap/" (drop-leading-slash "//path/htap/"))))


(deftest test-path
  (is (= "uri/path" (path (mock-request :uri "uri/path"))))
  (is (= "path-info/path" (path (mock-request :path-info "path-info/path"))))
  (is (= "path-info/path" (path (mock-request :path-info "path-info/path" :uri "uri/path")))))


(deftest test-process-name
  (is (= "get-test-path" (process-name "get" "/test/path/")))
  (is (= "get-test-path" (process-name "get" "test/path")))
  (is (= "get" (process-name "get" nil))))


(deftest test-request->process-name
  (is (= "get-test-path" 
         (request->process-name (mock-request :uri "/test/path" :request-method :get))))
  (is (= "get-test-path-info" 
         (request->process-name 
           (mock-request :path-info "/test/path/info":uri "/test/path" :request-method :get)))))


(deftest test-keyify-params)

(deftest test-json-requested?)

(deftest test-clojure-requested?)

(deftest test-form-request?)

