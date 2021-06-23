package com.pergam.courses.httpserver.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HttpCommonRegexTest {

    @Test
    void multipartContentDisposition_applyToMatchingString_returnsRegexGroups() {
        // given
        String matchingString = "Content-Disposition: form-data";

        String[] expectedGroupValues = {matchingString};

        // when
        String[] actualGroupValues = HttpCommonRegex.MULTIPART_CONTENT_DISPOSITION_FORM_DATA_REGEX.apply(matchingString);

        //then
        assertArrayEquals(expectedGroupValues, actualGroupValues);
    }

    @Test
    void multipartContentDisposition_applyToNonMatchingString_returnsRegexGroups() {
        // given
        String nonMatchingString = "Content-Disposition: spider-pig";

        // when
        String[] actualGroupValues = HttpCommonRegex.MULTIPART_CONTENT_DISPOSITION_FORM_DATA_REGEX.apply(nonMatchingString);

        //then
        assertNull(actualGroupValues);
    }

    @Test
    void multipartName_applyToMatchingString_returnsRegexGroups() {
        // given
        String matchingString = "name=\"someName\"";

        String[] expectedGroupValues = {matchingString, "someName"};

        // when
        String[] actualGroupValues = HttpCommonRegex.MULTIPART_NAME_REGEX.apply(matchingString);

        //then
        assertArrayEquals(expectedGroupValues, actualGroupValues);
    }

    @Test
    void multipartName_applyToNonMatchingString_returnsRegexGroups() {
        // given
        String nonMatchingString = "name=\"\"";

        // when
        String[] actualGroupValues = HttpCommonRegex.MULTIPART_NAME_REGEX.apply(nonMatchingString);

        //then
        assertNull(actualGroupValues);
    }

    @Test
    void multipartFileName_applyToMatchingString_returnsRegexGroups() {
        // given
        String matchingString = "filename=\"foo.txt\"";

        String[] expectedGroupValues = {matchingString, "foo.txt"};

        // when
        String[] actualGroupValues = HttpCommonRegex.MULTIPART_FILE_NAME_REGEX.apply(matchingString);

        //then
        assertArrayEquals(expectedGroupValues, actualGroupValues);
    }

    @Test
    void multipartFileName_applyToNonMatchingString_returnsRegexGroups() {
        // given
        String nonMatchingString = " filename=\"\"";

        // when
        String[] actualGroupValues = HttpCommonRegex.MULTIPART_FILE_NAME_REGEX.apply(nonMatchingString);

        //then
        assertNull(actualGroupValues);
    }

    // ---- throw-exception behavior is common for all regex enums so we test only one enum ----

    @Test
    void multipartContentDisposition_applyToNonMatchingStringWithExceptionFunction_throwsException() {
        // given
        String nonMatchingString = "";
        HttpCommonRegex.ThrowExceptionFunction throwExceptionFunction = () -> {
            throw new RuntimeException("Test...");
        };

        // when & then
        Exception exception = assertThrows(RuntimeException.class,
                () -> HttpCommonRegex.MULTIPART_CONTENT_DISPOSITION_FORM_DATA_REGEX.apply(nonMatchingString, throwExceptionFunction));
        assertEquals("Test...", exception.getMessage());
    }
}
