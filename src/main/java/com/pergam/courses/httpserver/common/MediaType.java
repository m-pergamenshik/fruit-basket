package com.pergam.courses.httpserver.common;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum MediaType {

    MULTIPART_FORM_DATA("multipart/form-data"),
    APPLICATION_FORM_URL_ENCODED("application/x-www-form-urlencoded");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
