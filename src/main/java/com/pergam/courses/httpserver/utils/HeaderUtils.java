package com.pergam.courses.httpserver.utils;

import java.util.Map;
import java.util.Set;

import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.CRLF;
import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.HEADER_KEY_VALUE_SEPARATOR;
import static java.util.stream.Collectors.toSet;

public class HeaderUtils {

    public static int findHeaderLastBytePosition(byte[] bytes, int bytesRead) {
        int bytePosition = 0;
        while (bytePosition + 3 < bytesRead) {
            if (bytes[bytePosition] == '\r'
                    && bytes[bytePosition + 1] == '\n'
                    && bytes[bytePosition + 2] == '\r'
                    && bytes[bytePosition + 3] == '\n') { // we do not tolerate "\n\n"
                return bytePosition + 3;
            }
            bytePosition++;
        }
        return -1;
    }

    public static String convertToMultilineString(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }
        Set<String> headerLines = headers.entrySet().stream()
                .map(e -> e.getKey().strip() + HEADER_KEY_VALUE_SEPARATOR + " " + e.getValue())
                .collect(toSet());
        return String.join(CRLF, headerLines);
    }
}
