package com.pergam.courses.httpserver.request.body.urlencoded;

import com.pergam.courses.httpserver.request.body.HttpRequestBody;
import com.pergam.courses.httpserver.utils.ParamsStringDecoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * For simplicity we are not implementing proper decoding of escaped (url-encoded) chars
 */
public class UrlEncodedBodyParser {

    public static HttpRequestBody parseUrlEncodedBody(InputStream savedBodyInputStream) throws IOException {
        String bodyAsString = new String(savedBodyInputStream.readAllBytes()); // we assume urlEncoded body is small enough to fit into heap
        Map<String, String> params = ParamsStringDecoder.decodeParamsString(bodyAsString);
        return HttpRequestBody.forUrlEncodedType(params);
    }

}
