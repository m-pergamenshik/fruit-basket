package com.pergam.courses.httpserver.request;

import com.pergam.courses.httpserver.handler.HttpEndpointKey;
import com.pergam.courses.httpserver.handler.HttpRequestHandler;
import com.pergam.courses.httpserver.response.HttpResponse;
import com.pergam.courses.httpserver.response.HttpResponseStatus;
import com.pergam.courses.httpserver.response.HttpResponseStatusLine;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.DOUBLE_CRLF;

@RequiredArgsConstructor
public class AsyncRequestHandler implements Runnable {

    private static final Map<String, String> HEADERS_CONTENT_TYPE_TEXT = Map.of("Content-Type", "text/plain; charset=UTF-8");

    private final Socket socket;
    private final HttpRequestParser httpRequestParser;
    private final Map<HttpEndpointKey, HttpRequestHandler> endpointsToRequestHandlers;
    private OutputStream outputStream;

    @Override
    public void run() {
        /**
         * Closing this.socket will also close the derived input/output streams.
         * We auto-close the socket via a try-with-resources block because we assume that either a proper or an error
         * response will be sent before we exit the block.
         */
        try (this.socket) { // since Java 9 we can place previously init but effectively final resources in try-with-resources (http://openjdk.java.net/jeps/213)
            InputStream inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            try {
                HttpRequest request = this.httpRequestParser.parseRequest(inputStream);
                HttpEndpointKey endpointKey = new HttpEndpointKey(request.getHttpMethod(), request.getUrl());
                HttpRequestHandler requestHandler = this.endpointsToRequestHandlers.get(endpointKey);
                if (requestHandler == null) {
                    throw new BadRequestException(String.format("No handler for request: %s %s", request.getHttpMethod(), request.getUrl()));
                }
                HttpResponse response = (HttpResponse) requestHandler.getMethod().invoke(requestHandler.getHandlerInstance(), request);
                sendResponse(response);
            } catch (Exception e) {
                sendErrorResponse(e);
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendResponse(HttpResponse response) throws IOException {
        this.outputStream.write(response.getResponseHeadAsBytes());
        InputStream bodyInputStream = response.getBodyInputStream();
        if (bodyInputStream != null) {
            try (bodyInputStream) {
                bodyInputStream.transferTo(this.outputStream); // transferTo() does buffered writes to outputStream
            }
        }
    }

    private void sendErrorResponse(Exception exception) throws IOException {
        HttpResponseStatus httpResponseStatus;
        if (exception instanceof BadRequestException) {
            httpResponseStatus = HttpResponseStatus.BAD_REQUEST;
        } else {
            httpResponseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        }
        HttpResponseStatusLine statusLine = new HttpResponseStatusLine(httpResponseStatus);
        String humanReadableError = httpResponseStatus + DOUBLE_CRLF + exception.getMessage();
        HttpResponse response = new HttpResponse(statusLine, HEADERS_CONTENT_TYPE_TEXT, humanReadableError);
        sendResponse(response);
    }
}
