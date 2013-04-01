# About

WebPoint is a small Clojure library that provides endpoints to web requests by mapping the method and path of the request to a specific function that takes the request.  Built on top of some really great libraries, such as [ring](https://github.com/ring-clojure) and [compojure](https://github.com/weavejester/compojure), WebPoint was designed to provide a specific request mapping pattern that, when combined with the [Pliant Process](https://github.com/pliant/process) library, makes it extremely easy to create pliant, extensible web applications.

## Use Case For Extensibility
So why would you want an extensible web application?  Extensibility should be a part any product that is built and sold.  Sure, selling rigid software allows you to bill the heck out of your customers whenever they want any little thing changed, but at that point you are doing something TO your customer rather than FOR them.  Extensibility allows you to customize your application/product for different customers without changing the core product.  Extensibility allows you to provide a shareable library of plugins to extend the application.  Most importantly, the management of customization of your application is external to the core application, leaving behind the need to fork or branch your code, allowing you to just manage that specific customization in it's own packaging.

## Usage

To use WebPoint, you need to let ring know about the routes provided by WebPoint, and then map your functions to the WebPoint endpoints.  These examples assume that your are using lein-ring to map your application to the JEE servlet runtime and using the Pliant Process library to create your mapped functions.

### Discover Routes
You can set up you application in any way you choose that is capable of understanding compiled ring routes, but these examples assume that you are using lein-ring with a section in your project.clj file that looks like:

```clojure
:ring {:handler myorg.myapp.core/app :init myorg.myapp.core/init :destroy myorg.myapp.core/destroy}
```

With the project.clj file configured, our /myorg/myapp.clj code would look like:

```clojure
(ns myorg.myapp.core
  (:require [pliant.configure.runtime :as runtime]
            [pliant.webpoint.request :as request])
  (:use [myorg.myapp.process]))

(defn init
  []
  (runtime/load-resources "myorg/myapp/bootstrap.clj"))

(defn destroy [])

(def app request/routes)
```

This is WebPoint at it's simplest. No middleware, just routes.  You can also add middleware as you would any other ring application.

I've also added calling ``pliant.configure.runtime/load-resources`` from the [Pliant Configure](https://github.com/pliant/configure) project as a way to bootstrap external plugins/modules to my application, but that is another story.

### Mapping Functions To Endpoints
Mapping functions to endpoints only requires you create a method that is registered with the multimethod found at ``pliant.webpoint.request/endpoints``.  For example

```clojure
(ns myorg.myapp.methods
  (:require [pliant.webpoint.request :as request]))

(defmethod pliant.webpoint.request/endpoints "get"
  [request]
  (wrap-with-html-yadayada "Welcome To The Home Page!"))
```

The ``get`` dispatch value represents a request to the root directory of your application. The dispatch function uses the request method and path to create the value to dispatch on, replacing all ``/`` characters in the path with ``-``.  So, a ``POST`` to ``/api/users`` would match a function that dispatches on ``post-api-users``.  WebPoint takes the context path into consideration when creating the dispatch value.  Applications shouldn't be limited by their code in how they can be deployed.  While ring does not take the context path into account, lein-ring does make the context path available in the request in the :context entry and the rest of the path in the :path-info entry of the request map\(just another reason you should use lein-ring\).  An application deployed at root ``/`` or deployed at the path ``/myapp`` will have the same dispatch value if the context and path-info are made available, else the URI is used to create the dispatch value, and bad things can happen.

While the defmethod can map a function for you, mapping to processes using the Pliant Process library can give you extensibility:

```clojure
(ns myorg.myapp.process
  (:require [pliant.webpoint.request :as request])
  (:use     [pliant.process :only [defprocess as-method]]))

(defprocess view-home
  [request]
  (wrap-with-html-yadayada "Welcome To The Home Page!"))

(as-method view-home request/endpoints "get")
```

OK, seems to be two more lines of code, so why use process, and WebPoint, for that matter.  Since we are now using a process rather than a method, we can create layers to the process that can create a completely different home page when some context is true.  For example, if you create a file called ``bootstrap.clj`` and save it in the ``/myorg/myapp`` folder in your classpath, you can change the home page to look differently on the weekends:

```clojure
(ns myorg.myapp.bootstrap
  (:require [myorg.myapp.process :as myapp])
  (:use     [pliant.process :only [deflayer]]))

(deflayer myapp/view-home weekend-home-page
  [request]
  (if (weekend?)
    (wrap-with-html-yadayada "Why are you on the web?  Go have fun.")
    (continue)))
```

You can have one hundred different deployments, each with the same application deployed, and they all have different home pages because each has a created it's own plugin library that is bootstrapped in on startup, all without having to fork an entire project or create a specific branch for those customers.  While modifying the home page is a simple example, it extends to all requests and workflow within your application.

## License

Copyright © 2012-2013

Distributed under the Eclipse Public License.
