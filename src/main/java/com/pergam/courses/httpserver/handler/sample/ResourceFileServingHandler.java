package com.pergam.courses.httpserver.handler.sample;

import com.pergam.courses.httpserver.handler.HttpEndpoint;
import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;
import com.pergam.courses.httpserver.response.HttpResponseStatus;
import com.pergam.courses.httpserver.response.HttpResponseStatusLine;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.pergam.courses.httpserver.FileUtils.getResourceFileAsInputStream;

@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ResourceFileServingHandler {

    private final String resourceFileName;
    private final String contentTypeHeaderValue;

    @HttpEndpoint(method = HttpMethod.GET, url = "/download-resource-file")
    public HttpResponse serveResourceFileForDownloading(HttpRequest request) throws IOException {
        HttpResponseStatusLine okResponseStatus = new HttpResponseStatusLine(HttpResponseStatus.OK);
        InputStream fileInputStream = getResourceFileAsInputStream(this.resourceFileName);
        Map<String, String> headers = Map.of(
                "Content-Disposition", "attachment; filename=\"" + this.resourceFileName + "\"",
                "Content-Length", String.valueOf(fileInputStream.available()),
                "Content-Type", this.contentTypeHeaderValue);
        return new HttpResponse(okResponseStatus, headers, fileInputStream);
    }
}
