package com.pergam.courses.httpserver.handler.sample;

import com.pergam.courses.httpserver.handler.HttpEndpoint;
import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;
import com.pergam.courses.httpserver.response.HttpResponseStatus;
import com.pergam.courses.httpserver.response.HttpResponseStatusLine;

import java.util.Map;

import static com.pergam.courses.httpserver.FileUtils.getResourceFileAsString;

@SuppressWarnings("unused")
public class UrlEncodedFormHandler {

    private static final HttpResponseStatusLine OK_HTTP_RESPONSE_STATUS_LINE = new HttpResponseStatusLine(HttpResponseStatus.OK);
    private static final Map<String, String> HEADERS_CONTENT_TYPE_HTML = Map.of("Content-type", "text/html; charset=UTF-8");

    @HttpEndpoint(method = HttpMethod.GET, url = "/urlencoded")
    public HttpResponse getMultipart(HttpRequest request) {
        String body = getResourceFileAsString("form_urlencoded.html")
                .replace("%%formAction%%", "urlencoded");
        return new HttpResponse(OK_HTTP_RESPONSE_STATUS_LINE, HEADERS_CONTENT_TYPE_HTML, body);
    }

    @HttpEndpoint(method = HttpMethod.POST, url = "/urlencoded")
    public HttpResponse postMultipart(HttpRequest request) {
        String body = getResourceFileAsString("form_submission_result.html")
                .replace("%%responseBody%%", request.toString().trim());
        return new HttpResponse(OK_HTTP_RESPONSE_STATUS_LINE, HEADERS_CONTENT_TYPE_HTML, body);
    }
}
