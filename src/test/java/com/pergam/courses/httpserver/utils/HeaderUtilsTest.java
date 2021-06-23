package com.pergam.courses.httpserver.utils;

import com.pergam.courses.httpserver.FileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HeaderUtilsTest {

    @Test
    void validHeaderBytes_findHeaderLastBytePosition_findsCorrectPosition() {
        // given
        byte[] validHeaderBytes = FileUtils.getResourceFileAsBytes("multipart_boundary_parsing/all_valid.txt");

        // when
        int actualHeaderEndPosition = HeaderUtils.findHeaderLastBytePosition(validHeaderBytes, validHeaderBytes.length);

        // then
        assertEquals(100, actualHeaderEndPosition);
    }
}
