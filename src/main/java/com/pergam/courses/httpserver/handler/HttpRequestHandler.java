package com.pergam.courses.httpserver.handler;

import lombok.Value;

import java.lang.reflect.Method;

@Value
public class HttpRequestHandler {
    Method method;
    Object handlerInstance;
}
