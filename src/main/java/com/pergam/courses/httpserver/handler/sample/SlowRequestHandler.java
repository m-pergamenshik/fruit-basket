package com.pergam.courses.httpserver.handler.sample;

import com.pergam.courses.httpserver.handler.HttpEndpoint;
import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;
import com.pergam.courses.httpserver.response.HttpResponseStatus;
import com.pergam.courses.httpserver.response.HttpResponseStatusLine;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("unused")
public class SlowRequestHandler {

    private static final HttpResponseStatusLine OK_HTTP_RESPONSE_STATUS_LINE = new HttpResponseStatusLine(HttpResponseStatus.OK);
    private static final Map<String, String> HEADERS_CONTENT_TYPE_TEXT = Map.of("Content-type", "text/plain; charset=UTF-8");

    @HttpEndpoint(method = HttpMethod.GET, url = "/slow")
    public HttpResponse handleGetRequest(HttpRequest request) throws IOException, InterruptedException {

        String sleepParam = request.getParams().get("sleep");
        long sleep;
        if (sleepParam != null) {
            sleep = Long.parseLong(request.getParams().get("sleep"));
        } else {
            sleep = 3000;
        }
        Thread.sleep(sleep);

        String responseMessage = String.format("Request processed by thread %s in %d ms", Thread.currentThread().getName(), sleep);

        return new HttpResponse(OK_HTTP_RESPONSE_STATUS_LINE, HEADERS_CONTENT_TYPE_TEXT, responseMessage);
    }

}
