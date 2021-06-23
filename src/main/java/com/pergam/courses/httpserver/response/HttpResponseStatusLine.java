package com.pergam.courses.httpserver.response;

import com.pergam.courses.httpserver.HttpServer;
import lombok.Value;

@Value
public class HttpResponseStatusLine {

    String version;
    HttpResponseStatus responseStatus;

    public HttpResponseStatusLine(HttpResponseStatus responseStatus) {
        this.version = HttpServer.DEFAULT_HTTP_PROTOCOL_VERSION;
        this.responseStatus = responseStatus;
    }

    @Override
    public String toString() {
        return version + " " + responseStatus;
    }
}
