package com.pergam.courses.httpserver.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum HttpHeader {

    CONTENT_LENGTH("content-length"),
    CONTENT_TYPE("content-type");

    public final String value;

    @Override
    public String toString() {
        return value;
    }
}
