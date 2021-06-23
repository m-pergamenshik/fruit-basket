package com.pergam.courses.httpserver.handler.sample;

import com.pergam.courses.httpserver.handler.HttpEndpoint;
import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;
import com.pergam.courses.httpserver.response.HttpResponseStatus;
import com.pergam.courses.httpserver.response.HttpResponseStatusLine;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static com.pergam.courses.httpserver.FileUtils.getLocalFileAsInputStream;

@RequiredArgsConstructor
public class LocalFileServingHandler {

    private final File localFile;
    private final String contentTypeHeaderValue;

    @SuppressWarnings("unused")
    @HttpEndpoint(method = HttpMethod.GET, url = "/download-local-file")
    public HttpResponse serveResourceFileForDownloading(HttpRequest request) throws IOException {
        HttpResponseStatusLine okResponseStatus = new HttpResponseStatusLine(HttpResponseStatus.OK);
        InputStream fileInputStream = getLocalFileAsInputStream(this.localFile);
        Map<String, String> headers = Map.of(
                "Content-Disposition", "attachment; filename=\"" + this.localFile.getName() + "\"",
                "Content-Length", String.valueOf(fileInputStream.available()),
                "Content-Type", this.contentTypeHeaderValue);
        return new HttpResponse(okResponseStatus, headers, fileInputStream);
    }
}
