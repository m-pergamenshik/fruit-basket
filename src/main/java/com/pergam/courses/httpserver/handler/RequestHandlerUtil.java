package com.pergam.courses.httpserver.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;
import com.pergam.courses.httpserver.response.HttpResponseStatus;
import com.pergam.courses.httpserver.response.HttpResponseStatusLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class RequestHandlerUtil {

    private static final Map<String, String> HEADERS_CONTENT_TYPE_JSON = Map.of("Content-type", "application/json");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final HttpResponseStatusLine OK_RESPONSE_STATUS_LINE = new HttpResponseStatusLine(HttpResponseStatus.OK);

    static {
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static HttpResponse responseWithRequestAsJson(HttpRequest request) throws IOException {
        ObjectNode requestObjectNode = OBJECT_MAPPER.valueToTree(request);
        InputStream nonFormBodyInputStream = request.getNonFormBodyInputStream();
        if (nonFormBodyInputStream != null) {
            requestObjectNode.put("nonFormBodyDataSize", nonFormBodyInputStream.available());
        }
        return new HttpResponse(OK_RESPONSE_STATUS_LINE, HEADERS_CONTENT_TYPE_JSON, requestObjectNode.toString());
    }
}
