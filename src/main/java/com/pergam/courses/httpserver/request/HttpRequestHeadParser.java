package com.pergam.courses.httpserver.request;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.*;
import static com.pergam.courses.httpserver.utils.ParamsStringDecoder.decodeParamsString;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public final class HttpRequestHeadParser {

    private static final int HTTP_START_LINE_PARTS = 3;
    private static final int HTTP_METHOD_POSITION = 0;
    private static final int HTTP_URL_POSITION = 1;
    private static final int HTTP_PROTOCOL_VERSION_POSITION = 2;

    public static HttpRequestHead parse(String requestHead) {
        List<String> requestHeadLines = requestHead.lines().collect(toList());
        HttpRequestStartLine startLine = parseStartLine(requestHeadLines.get(0));
        Map<String, String> headers = requestHeadLines
                .stream()
                .skip(1) // skipping start line
                .map(Header::new)
                .collect(toMap(Header::getName, Header::getValue));
        return new HttpRequestHead(startLine, headers);
    }

    private static HttpRequestStartLine parseStartLine(String startLine) {
        String[] methodAndUrlAndVersion = startLine.split(" ", HTTP_START_LINE_PARTS); // for simplicity here we avoid complex regex (but we use regex elsewhere)
        if (methodAndUrlAndVersion.length != HTTP_START_LINE_PARTS) {
            throw new BadRequestException(format("Start line must consist of %d parts", HTTP_START_LINE_PARTS));
        }
        final String httpMethodRaw = methodAndUrlAndVersion[HTTP_METHOD_POSITION];
        HttpMethod method;
        try {
            method = HttpMethod.valueOf(httpMethodRaw);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unsupported HTTP method: " + httpMethodRaw);
        }
        String rawUrlWithParams = methodAndUrlAndVersion[HTTP_URL_POSITION];
        String[] urlWithParamsSplit = rawUrlWithParams.split("\\?", 2);
        String url;
        Map<String, String> queryParams;
        if (urlWithParamsSplit.length == 2) {
            url = urlWithParamsSplit[0];
            queryParams = decodeParamsString(urlWithParamsSplit[1]);
        } else {
            url = rawUrlWithParams;
            queryParams = Collections.emptyMap();
        }
        String version = methodAndUrlAndVersion[HTTP_PROTOCOL_VERSION_POSITION];
        return new HttpRequestStartLine(method, url, queryParams, version);
    }

    @Getter
    private static class Header {

        private final String name;
        private final String value;

        public Header(String rawHeaderLine) {
            assertNotNullOrBlank(rawHeaderLine);
            String[] valueAndName = rawHeaderLine.split(HEADER_KEY_VALUE_SEPARATOR, 2);
            assertHeaderLineSplitInTwo(valueAndName);
            this.name = valueAndName[0].toLowerCase();
            this.value = valueAndName[1].strip(); // strip() is Java 11, replaces trim() https://stackoverflow.com/questions/51266582/difference-between-string-trim-and-strip-methods-in-java-11
        }

        private static void assertNotNullOrBlank(String headerLine) {
            if (headerLine == null || headerLine.isBlank()) { // isBlank() is Java 11, preferred to isEmpty() https://stackoverflow.com/questions/23419087/stringutils-isblank-vs-string-isempty
                throw new IllegalArgumentException("Header line is empty");
            }
        }

        private static void assertHeaderLineSplitInTwo(String[] valueAndName) {
            if (valueAndName.length < 2) {
                throw new IllegalArgumentException("Header line does not contain separator " + HEADER_KEY_VALUE_SEPARATOR);
            }
        }
    }
}
