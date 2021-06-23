package com.pergam.courses.httpserver;

import com.pergam.courses.httpserver.handler.HttpEndpointKey;
import com.pergam.courses.httpserver.handler.HttpRequestHandler;
import com.pergam.courses.httpserver.request.AsyncRequestHandler;
import com.pergam.courses.httpserver.request.HttpRequestParser;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.pergam.courses.httpserver.handler.HttpHandlerRegistration.mapEndpointsToRequestHandlers;

/**
 * TOp recommended book: Java Network Programming, 4th Edition. by Elliotte Rusty Harold
 */
public class HttpServer implements Runnable {

    public static final String DEFAULT_HTTP_PROTOCOL_VERSION = "HTTP/1.1";
    private final int listeningPort;
    private final Map<HttpEndpointKey, HttpRequestHandler> endpointsToRequestHandlers;
    private ExecutorService requestHandlingThreadPool;
    private final HttpRequestParser httpRequestParser;
    private volatile boolean isShutDown = false; // volatile to allow loop in run() to pick up change immediately

    public HttpServer(int listeningPort, HttpServerConfig serverConfig, Object... requestHandlers) {
        this.listeningPort = listeningPort;
        this.endpointsToRequestHandlers = mapEndpointsToRequestHandlers(requestHandlers);
        this.httpRequestParser = new HttpRequestParser(serverConfig);
    }

    public void start() {
        this.requestHandlingThreadPool = Executors.newCachedThreadPool();
        Thread serverMainThread = new Thread(this, "http-server-main-thread");
        serverMainThread.start();
        System.out.println(serverMainThread.getName() + " has started");
    }

    public void shutDown() {
        this.isShutDown = true; // must be first to avoid creating new sockets after thread pool is shut down
        this.requestHandlingThreadPool.shutdown();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(this.listeningPort)) { // good explanation of ServerSocket constructor params: http://underpop.online.fr/j/java/help/serversocket-class-network-dev-java-programming-language.html.gz
            while (!isShutDown) {
                Socket socket = serverSocket.accept();
                AsyncRequestHandler asyncRequestHandler =
                        new AsyncRequestHandler(socket, this.httpRequestParser, this.endpointsToRequestHandlers);
                this.requestHandlingThreadPool.execute(asyncRequestHandler);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
