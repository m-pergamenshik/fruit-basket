package com.pergam.courses.httpserver.handler;

import com.pergam.courses.httpserver.request.HttpMethod;
import lombok.Value;

@Value
public class HttpEndpointKey {
    HttpMethod httpMethod;
    String url;
}
