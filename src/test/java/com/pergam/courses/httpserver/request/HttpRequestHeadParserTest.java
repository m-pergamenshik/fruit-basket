package com.pergam.courses.httpserver.request;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.CRLF;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpRequestHeadParserTest {

    @Test
    void shouldParseValidHttpRequestHead() {
        // given
        final String VALID_POST_REQUEST_WITH_BODY =
                "POST /some/path?key1=value1&key2=value2 HTTP/1.1".concat(CRLF)
                        .concat("Host: localhost:8080").concat(CRLF)
                        .concat("Content-length: 21");

        // when
        HttpRequestHead actualHttpRequestHead = HttpRequestHeadParser.parse(VALID_POST_REQUEST_WITH_BODY);

        // then
        assertEquals(expectedHttpRequestHead(), actualHttpRequestHead);
    }

    private static HttpRequestHead expectedHttpRequestHead() {
        HttpMethod method = HttpMethod.POST;
        String url = "/some/path";
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("key1", "value1");
        queryParams.put("key2", "value2");
        String version = "HTTP/1.1";
        HttpRequestStartLine startLine = new HttpRequestStartLine(method, url, queryParams, version);
        Map<String, String> headers = new HashMap<>();
        headers.put("host", "localhost:8080");
        headers.put("content-length", "21");
        return new HttpRequestHead(startLine, headers);
    }
}
