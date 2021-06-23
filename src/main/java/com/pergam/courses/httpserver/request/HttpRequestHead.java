package com.pergam.courses.httpserver.request;

import com.pergam.courses.httpserver.utils.HeaderUtils;
import lombok.Value;

import java.util.Map;

import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.CRLF;

@Value
public class HttpRequestHead {
    HttpRequestStartLine startLine;
    Map<String, String> headers;

    @Override
    public String toString() {
        return startLine + CRLF + HeaderUtils.convertToMultilineString(headers);
    }
}
