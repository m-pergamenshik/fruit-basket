package com.pergam.courses.httpserver.request.body.multipart;

import com.pergam.courses.httpserver.request.BadRequestException;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.pergam.courses.httpserver.request.body.multipart.MultipartBoundaryLineSearch.getBoundaryLinePositions;
import static org.junit.jupiter.api.Assertions.*;

public class MultipartBodyParserTest {

    private static final String BOUNDARY = "98h9438hff93hf943f84hf948hf349f8h3498f3394f34hf984";

    @Test
    void multipartBodyWithValidBoundaries_getBoundaryLinePositions_returnsCorrectBoundaryLinePositions() throws IOException, URISyntaxException {
        // given
        ByteBuffer validMultipartBodyBuffer = getBodyBufferForLocalResource("multipart_boundary_parsing/all_valid.txt");
        int[] expectedBoundaryLinePositions = {0, 109, 306};

        // when
        int[] actualBoundaryLinePositions = getBoundaryLinePositions(validMultipartBodyBuffer, BOUNDARY);

        // then
        assertArrayEquals(expectedBoundaryLinePositions, actualBoundaryLinePositions);
    }

    @Test
    void multipartBodyWithLastBoundaryNotEndingWithTwoHyphens_getBoundaryLinePositions_throwsException() throws IOException, URISyntaxException {
        // given
        ByteBuffer multipartBodyWithInvalidLastBoundary =
                getBodyBufferForLocalResource("multipart_boundary_parsing/last_boundary_not_ending_with_two_hyphens.txt");

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> getBoundaryLinePositions(multipartBodyWithInvalidLastBoundary, BOUNDARY));

        assertEquals("Multipart body is missing closing boundary (post-fixed with two hyphens)", badRequestException.getMessage());
    }

    @Test
    void multipartBodyWithLessThanTwoBoundaries_getBoundaryLinePositions_throwsException() throws IOException, URISyntaxException {
        // given
        ByteBuffer multipartBodyWithInvalidLastBoundary =
                getBodyBufferForLocalResource("multipart_boundary_parsing/less_than_2_boundaries.txt");

        // when & then
        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> getBoundaryLinePositions(multipartBodyWithInvalidLastBoundary, BOUNDARY));

        assertEquals("Multipart body contains less than two boundaries", badRequestException.getMessage());
    }

    private ByteBuffer getBodyBufferForLocalResource(String resourceName) throws IOException, URISyntaxException {
        File testFile = new File(getClass().getClassLoader().getResource(resourceName).toURI());
        return new FileInputStream(testFile).getChannel().map(FileChannel.MapMode.READ_ONLY, 0, testFile.length());
    }
}
