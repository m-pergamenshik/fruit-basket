package com.pergam.courses.httpserver.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContentTypeTest {

    @Test
    public void validFormUrlEncodedContentTypeValue_newContentType_parsesCorrectly() {
        // given
        String validMultipartContentType = "application/x-www-form-urlencoded";
        ContentType expectedContentType = new ContentType(MediaType.APPLICATION_FORM_URL_ENCODED.toString(), null);

        // when
        ContentType actualContentType = new ContentType(validMultipartContentType);

        // then
        assertEquals(expectedContentType, actualContentType);
    }

    @Test
    public void validMultipartContentTypeValueWithBoundary_newContentType_parsesCorrectly() {
        // given
        String validMultipartContentType = "multipart/form-data; boundary=------974767299852498929531610575";
        ContentType.OptionalParam optionalContentTypeParam = new ContentType.OptionalParam("boundary", "------974767299852498929531610575");
        ContentType expectedContentType = new ContentType(MediaType.MULTIPART_FORM_DATA.toString(), optionalContentTypeParam);

        // when
        ContentType actualContentType = new ContentType(validMultipartContentType);

        // then
        assertEquals(expectedContentType, actualContentType);
    }

    @Test
    public void malformedMediaTypeInContentTypeValue_newContentType_throwsRuntimeException() {
        // given
        String malformedMediaTypeInContentTypeValue = "malformed-media-type; charset=UTF-8";

        // when && then
        Exception exception = assertThrows(RuntimeException.class,
                () -> new ContentType(malformedMediaTypeInContentTypeValue));

        assertTrue(exception.getMessage().startsWith("Unexpected media type format:"));
    }

    @Test
    public void multipartContentTypeValueWithoutBoundaryParam_newContentType_throwsRuntimeException() {
        // given
        String multipartContentTypeWithoutBoundaryParam = "multipart/form-data;";

        // when && then
        Exception exception = assertThrows(RuntimeException.class,
                () -> new ContentType(multipartContentTypeWithoutBoundaryParam));

        assertEquals("Multipart boundary not defined in Content-type header", exception.getMessage());
    }

    @Test
    public void multipartContentTypeValueWithWrongParam_newContentType_throwsRuntimeException() {
        // given
        String multipartContentTypeValueWithWrongParam = "multipart/form-data; charset=UTF-8";

        // when && then
        Exception exception = assertThrows(RuntimeException.class,
                () -> new ContentType(multipartContentTypeValueWithWrongParam));

        assertTrue(exception.getMessage().startsWith("Multipart boundary not defined in Content-type header"));
    }
}
