package com.pergam.courses.httpserver.response;

import com.pergam.courses.httpserver.utils.HeaderUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.CRLF;
import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.DOUBLE_CRLF;

@RequiredArgsConstructor
@Getter
public class HttpResponse {
    private final HttpResponseStatusLine responseStatusLine;
    private final Map<String, String> headers;
    private final InputStream bodyInputStream;

    /**
     * @param textBody we assume it's UTF-8 encoded and small enough to fit in memory
     */
    public HttpResponse(HttpResponseStatusLine responseStatusLine, Map<String, String> headers, String textBody) {
        this.responseStatusLine = responseStatusLine;
        this.headers = headers;
        byte[] textBodyBytes = textBody.getBytes(StandardCharsets.UTF_8); // for simplicity we assume UTF-8
        this.bodyInputStream = new ByteArrayInputStream(textBodyBytes);
    }

    public byte[] getResponseHeadAsBytes() {
        final String headersAsString = (this.headers != null && !this.headers.isEmpty()) ?
                CRLF + HeaderUtils.convertToMultilineString(this.headers) : "";

        return (this.responseStatusLine
                + headersAsString
                + DOUBLE_CRLF)
                .getBytes(StandardCharsets.US_ASCII);
    }
}
