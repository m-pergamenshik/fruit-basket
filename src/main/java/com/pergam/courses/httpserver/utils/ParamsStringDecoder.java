package com.pergam.courses.httpserver.utils;

import java.util.HashMap;
import java.util.Map;

public class ParamsStringDecoder {

    /**
     * We use String.split() instead of StringTokenizer:
     * - StringTokenizer is marginally faster for our purposes https://www.javamex.com/tutorials/regular_expressions/splitting_tokenisation_performance.shtml
     * - String.split() doesn't compile regex if it's only 1 char long (see source code for String::split)
     * - StringTokenizer is legacy (according to its own Javadoc)
     *
     * Also there is no standard to handling duplicate keys (key1=A&key1=B)
     * https://stackoverflow.com/questions/1746507/authoritative-position-of-duplicate-http-get-query-keys
     * so we will take a simple approach and support last-given value
     */
    public static Map<String, String> decodeParamsString(String rawParamString) {
        String[] rawKeyValuePairs = rawParamString.split("&"); // we don't support & in query key/value
        Map<String, String> params = new HashMap<>(rawKeyValuePairs.length);
        for (String rawKeyValuePair : rawKeyValuePairs) {
            String[] keyValuePair = rawKeyValuePair.split("=", 2); // we split only by 1st "=" https://stackoverflow.com/a/42531514
            params.put(keyValuePair[0], keyValuePair[1]);
        }
        return params;
    }
}
