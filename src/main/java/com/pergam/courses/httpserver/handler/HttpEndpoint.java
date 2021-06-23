package com.pergam.courses.httpserver.handler;

import com.pergam.courses.httpserver.request.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpEndpoint {
    HttpMethod method();
    String url();
}
