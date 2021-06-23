package com.pergam.courses.httpserver.request.body;

import lombok.Getter;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Getter
public class HttpRequestBody {
    private final Map<String, String> params;
    private final Map<Path, String> uploadedFilesToContentTypes;
    private final InputStream nonFormBodyInputStream;

    private HttpRequestBody(Map<String, String> params, Map<Path, String> uploadedFilesToContentTypes, InputStream nonFormBodyInputStream) {
        this.params = params;
        this.uploadedFilesToContentTypes = uploadedFilesToContentTypes;
        this.nonFormBodyInputStream = nonFormBodyInputStream;
    }

    public static HttpRequestBody forUrlEncodedType(Map<String, String> params) {
        return new HttpRequestBody(params, emptyMap(), null);
    }

    public static HttpRequestBody forMultipartType(Map<String, String> params, Map<Path, String> uploadedFilesToContentTypes) {
        return new HttpRequestBody(params, uploadedFilesToContentTypes, null);
    }

    public static HttpRequestBody forNonFormBodyType(InputStream nonFormBodyInputStream) {
        return new HttpRequestBody(emptyMap(), emptyMap(), nonFormBodyInputStream);
    }
}
