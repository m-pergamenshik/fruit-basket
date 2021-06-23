package com.pergam.courses.httpserver.request;

import lombok.Value;

import java.util.Map;

@Value
public class HttpRequestStartLine {
    HttpMethod httpMethod;
    String url;
    Map<String, String> queryParams;
    String version;
}
