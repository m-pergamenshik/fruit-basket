package com.pergam.courses.httpserver.response;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum HttpResponseStatus {

    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    public final int statusCode;
    public final String statusMessage;


    @Override
    public String toString() {
        return this.statusCode + " " + statusMessage;
    }
}
