package com.pergam.courses.httpserver.request;

import com.pergam.courses.httpserver.request.body.HttpRequestBody;
import lombok.Value;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Value
public class HttpRequest {
    HttpMethod httpMethod;
    String url;
    String protocolVersion;
    Map<String, String> headers;
    Map<String, String> params;
    Map<Path, String> uploadedFilesToContentTypes;
    InputStream nonFormBodyInputStream;

    public HttpRequest(HttpRequestHead head, HttpRequestBody body) {
        HttpRequestStartLine startLine = head.getStartLine();
        this.httpMethod = startLine.getHttpMethod();
        this.url = startLine.getUrl();
        this.protocolVersion = startLine.getVersion();
        this.headers = head.getHeaders();
        if (body == null) {
            this.params = startLine.getQueryParams();
            this.uploadedFilesToContentTypes = null;
            this.nonFormBodyInputStream = null;
        } else {
            this.params = mergeParams(startLine.getQueryParams(), body.getParams());
            this.uploadedFilesToContentTypes = body.getUploadedFilesToContentTypes();
            this.nonFormBodyInputStream = body.getNonFormBodyInputStream();
        }
    }

    private static Map<String, String> mergeParams(Map<String, String> queryParams, Map<String, String> params) {
        Map<String, String> mergedMap = new HashMap<>(queryParams);
        mergedMap.putAll(params);
        return mergedMap;
    }
}

