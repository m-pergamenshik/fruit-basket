# HTTP server simulation

This is an HTTP server simulation which consists of a client and an HTTP multithreaded server "library" inspired by libraries like nanohttpd and servlet containers.

Initially it was written between April-June 2021 for a possible future video course but then repurposed to practice all sorts of low-level Java concepts and APIs as well as dive deeper into networking protocols (HTTP, TCP), network programming etc.

Obviously this is not a production-grade library. It is not even a proper Maven dependency. It just has the server part which acts like an HTTP server library and a client part which contains the main method and acts like the consumer of that library.

Main features:
* supports parsing HTTP requests with arbitrary headers and arbitrary body types and sizes (stores small bodies in memory, streams large bodies to disk), including saving files uploaded via form/multipart.
* supports producing HTTP responses with arbitrary headers and arbitrary body types and sizes, including enabling download of arbitrary files from disk.
* supports enabling HTTP endpoints with JAX-RS/Spring-like annotations used like this:
```
@HttpEndpoint(method = HttpMethod.GET, url = "/echo")
    public HttpResponse handleGetRequest(HttpRequest request) throws IOException {
        return RequestHandlerUtil.responseWithRequestAsJson(request);
    }
```

Because this project was meant to be a pure core Java exercise I avoided using external libraries or frameworks as much as possible, so I used only 3 libraries:
* Lombok
* JUnit
* Jackson (for tests only)

Project is build around the following core Java components:
* threads and thread pools
* socket, file and byte IO streams and channels
* reflection (for processing annotated methods)

Testing
* unit tests are available only for methods with the biggest cyclomatic complexy
* functional tests are based on sending HTTP requests both properly formed and malformed in all sorts of ways and ensuring proper response.

This HTTP server is based on blocking IO. Yes, I know that ideally we should use NIO or Netty. Maybe in the next project...
