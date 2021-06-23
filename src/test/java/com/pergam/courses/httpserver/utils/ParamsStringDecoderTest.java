package com.pergam.courses.httpserver.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParamsStringDecoderTest {

    @Test
    public void validQueryParamString_decode_returnsCorrectParamsMap() {
        // given
        String validQueryParamString = "param1=value1&param2=value2&param3=value3";
        Map<String, String> expectedParams = produceExpectedParams();

        // when
        Map<String, String> actualParams = ParamsStringDecoder.decodeParamsString(validQueryParamString);

        // then
        assertEquals(expectedParams, actualParams);
    }

    private static Map<String, String> produceExpectedParams() {
        Map<String, String> expectedParams = new HashMap<>(3);
        expectedParams.put("param1", "value1");
        expectedParams.put("param2", "value2");
        expectedParams.put("param3", "value3");
        return expectedParams;
    }
}
