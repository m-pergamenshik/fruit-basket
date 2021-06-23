package com.pergam.courses.httpserver.request.body;

import com.pergam.courses.httpserver.HttpServerConfig;
import com.pergam.courses.httpserver.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class HttpRequestBodySaverTest {

    private static byte[] requestBytes;
    private static byte[] expectedRequestBodyBytes;

    @BeforeAll
    static void setup() {
        requestBytes = FileUtils.getResourceFileAsBytes("body_saver_input/body_saver_input_307_bytes.txt");
        expectedRequestBodyBytes = FileUtils.getResourceFileAsBytes("body_saver_input/expected_body_231_bytes.txt");
    }

    @Test
    void maxInMemoryBodyStorageLessThanRequestBody_saveBodyAndReturnAsStream_returnsExpectedBodyBytesSavedAsFile() throws IOException {
        // given
        HttpServerConfig httpServerConfig = HttpServerConfig.builder()
                .requestFirstReadBufferLength(128)
                .requestBodyReadBufferLength(32)
                .maxInMemoryRequestBodyStorage(64)
                .build();
        HttpRequestBodySaver httpRequestBodySaver = new HttpRequestBodySaver(httpServerConfig);
        InputStream requestInputStream = new ByteArrayInputStream(requestBytes);

        final int bodyLength = 231;
        byte[] bytesFromFirstRead = requestInputStream.readNBytes(httpServerConfig.getRequestFirstReadBufferLength());
        final int headerEndBytePosition = 75;

        // when
        InputStream actualRequestBodyInputStream = httpRequestBodySaver.saveBodyAndReturnAsStream(requestInputStream, bodyLength, bytesFromFirstRead, bytesFromFirstRead.length, headerEndBytePosition);

        // then
        assertArrayEquals(expectedRequestBodyBytes, actualRequestBodyInputStream.readAllBytes());
        assertSame(actualRequestBodyInputStream.getClass(), FileInputStream.class);
    }

    @Test
    void maxInMemoryBodyStorageMoreThanRequestBody_saveBodyAndReturnAsStream_returnsExpectedBodyBytesSavedInMemory() throws IOException {
        // given
        HttpServerConfig httpServerConfig = HttpServerConfig.builder()
                .requestFirstReadBufferLength(128)
                .requestBodyReadBufferLength(32)
                .maxInMemoryRequestBodyStorage(256)
                .build();
        HttpRequestBodySaver httpRequestBodySaver = new HttpRequestBodySaver(httpServerConfig);
        InputStream requestInputStream = new ByteArrayInputStream(requestBytes);

        final int bodyLength = 231;
        byte[] bytesFromFirstRead = requestInputStream.readNBytes(httpServerConfig.getRequestFirstReadBufferLength());
        final int headerEndBytePosition = 75;

        // when
        InputStream actualRequestBodyInputStream = httpRequestBodySaver.saveBodyAndReturnAsStream(requestInputStream, bodyLength, bytesFromFirstRead, bytesFromFirstRead.length, headerEndBytePosition);

        // then
        assertArrayEquals(expectedRequestBodyBytes, actualRequestBodyInputStream.readAllBytes());
        assertSame(actualRequestBodyInputStream.getClass(), ByteArrayInputStream.class);
    }

    @Test
    void firstReadBufferMoreThanEntireRequest_saveBodyAndReturnAsStream_returnsExpectedBodyBytesSavedInMemory() throws IOException {
        // given
        HttpServerConfig httpServerConfig = HttpServerConfig.builder()
                .requestFirstReadBufferLength(512)
                .requestBodyReadBufferLength(32)
                .maxInMemoryRequestBodyStorage(256)
                .build();
        HttpRequestBodySaver httpRequestBodySaver = new HttpRequestBodySaver(httpServerConfig);
        InputStream requestInputStream = new ByteArrayInputStream(requestBytes);

        final int bodyLength = 231;
        byte[] bytesFromFirstRead = requestInputStream.readNBytes(httpServerConfig.getRequestFirstReadBufferLength());
        final int headerEndBytePosition = 75;

        // when
        InputStream actualRequestBodyInputStream = httpRequestBodySaver.saveBodyAndReturnAsStream(requestInputStream, bodyLength, bytesFromFirstRead, bytesFromFirstRead.length, headerEndBytePosition);

        // then
        assertArrayEquals(expectedRequestBodyBytes, actualRequestBodyInputStream.readAllBytes());
        assertSame(actualRequestBodyInputStream.getClass(), ByteArrayInputStream.class);
    }

    @Test
    void bodyReadBufferMoreThanRequestBody_saveBodyAndReturnAsStream_returnsExpectedBodyBytesSavedInMemory() throws IOException {
        // given
        HttpServerConfig httpServerConfig = HttpServerConfig.builder()
                .requestFirstReadBufferLength(128)
                .requestBodyReadBufferLength(256)
                .maxInMemoryRequestBodyStorage(256)
                .build();
        HttpRequestBodySaver httpRequestBodySaver = new HttpRequestBodySaver(httpServerConfig);
        InputStream requestInputStream = new ByteArrayInputStream(requestBytes);

        final int bodyLength = 231;
        byte[] bytesFromFirstRead = requestInputStream.readNBytes(httpServerConfig.getRequestFirstReadBufferLength());
        final int headerEndBytePosition = 75;

        // when
        InputStream actualRequestBodyInputStream = httpRequestBodySaver.saveBodyAndReturnAsStream(requestInputStream, bodyLength, bytesFromFirstRead, bytesFromFirstRead.length, headerEndBytePosition);

        // then
        assertArrayEquals(expectedRequestBodyBytes, actualRequestBodyInputStream.readAllBytes());
        assertSame(actualRequestBodyInputStream.getClass(), ByteArrayInputStream.class);
    }

}
