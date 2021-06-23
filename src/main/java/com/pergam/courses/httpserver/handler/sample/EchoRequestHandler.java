package com.pergam.courses.httpserver.handler.sample;

import com.pergam.courses.httpserver.handler.HttpEndpoint;
import com.pergam.courses.httpserver.handler.RequestHandlerUtil;
import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;

import java.io.IOException;


@SuppressWarnings("unused")
public class EchoRequestHandler {

    @HttpEndpoint(method = HttpMethod.GET, url = "/echo")
    public HttpResponse handleGetRequest(HttpRequest request) throws IOException {
        return RequestHandlerUtil.responseWithRequestAsJson(request);
    }

    @HttpEndpoint(method = HttpMethod.POST, url = "/echo")
    public HttpResponse handlePostRequest(HttpRequest request) throws IOException {
        return RequestHandlerUtil.responseWithRequestAsJson(request);
    }

}
