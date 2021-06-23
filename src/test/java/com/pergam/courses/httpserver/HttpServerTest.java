package com.pergam.courses.httpserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pergam.courses.httpserver.handler.sample.EchoRequestHandler;
import com.pergam.courses.httpserver.handler.sample.ResourceFileServingHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

import static com.pergam.courses.httpserver.FileUtils.getResourceFileAsBytes;
import static com.pergam.courses.httpserver.FileUtils.getResourceFileAsString;
import static com.pergam.courses.httpserver.common.HttpSpecialSymbols.DOUBLE_CRLF;
import static org.junit.jupiter.api.Assertions.*;

public class HttpServerTest {

    private static final int SERVER_PORT = 8080;
    private static ObjectMapper objectMapper;
    private static final String OK_200_JSON_RESPONSE_STATUS = "HTTP/1.1 200 OK\r\nContent-type: application/json";

    @BeforeAll
    public static void startServer() {
        Object[] requestHandlers = {
                new EchoRequestHandler(),
                new ResourceFileServingHandler("image_colors_234_bytes.png", "image/png")};
        HttpServerConfig defaultConfig = HttpServerConfig.defaultConfig();
        HttpServer httpServer = new HttpServer(SERVER_PORT, defaultConfig, requestHandlers);
        httpServer.start();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void validGetRequest_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("get_correct_request.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        String[] headerAndBody = response.split(DOUBLE_CRLF);
        assertEquals(OK_200_JSON_RESPONSE_STATUS, headerAndBody[0]);
        JsonNode requestJsonNode = parseJson(headerAndBody[1]);
        assertStatusLine("GET", "/echo", "HTTP/1.1", requestJsonNode);
        assertHeaders(Map.of("host", "localhost:8080", "user-agent", "qwerty"), requestJsonNode);
        assertParams(Map.of("param1", "value1", "param2", "value2"), requestJsonNode);
        assertEquals("null", requestJsonNode.path("uploadedFilesToContentTypes").asText());
        assertEquals("null", requestJsonNode.path("nonFormBodyInputStream").asText());
    }

    @Test
    public void validGetRequestToUnhandledUrl_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("get_unhandled_url_request.txt");
        String exceptedResponse = getResourceFileAsString("get_unhandled_url_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void validGetResourceFileDownloadRequest_sendToServer_getCorrectResponseWithFile() throws IOException {
        // given
        String request = getResourceFileAsString("get_correct_file_download_request.txt");
        byte[] expectedResponseHeadBytes = getResourceFileAsBytes("get_correct_file_download_response_head.txt");
        byte[] expectedResponseBodyBytes = getResourceFileAsBytes("image_colors_234_bytes.png");

        // when
        InputStream responseInputStream = sendRequestGetBackResponseInputStream(request);

        // then
        byte[] responseBytes = responseInputStream.readAllBytes();
        byte[] headBytes = Arrays.copyOfRange(responseBytes, 0, (responseBytes.length - 234));
        byte[] bodyBytes = Arrays.copyOfRange(responseBytes, (responseBytes.length - 234), responseBytes.length);
        assertArrayEquals(expectedResponseHeadBytes, headBytes);
        assertArrayEquals(expectedResponseBodyBytes, bodyBytes);
    }

    @Test
    public void getRequestWithInvalidHttpMethod_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("get_invalid_http_method_request.txt");
        String exceptedResponse = getResourceFileAsString("get_invalid_http_method_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void getRequestWithHeaderMissingSeparator_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("get_header_missing_separator_request.txt");
        String exceptedResponse = getResourceFileAsString("get_header_missing_separator_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void validPostRequestWithJson_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_json_correct_request.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        // then
        String[] headerAndBody = response.split(DOUBLE_CRLF);
        assertEquals(OK_200_JSON_RESPONSE_STATUS, headerAndBody[0]);
        JsonNode requestJsonNode = parseJson(headerAndBody[1]);
        assertStatusLine("POST", "/echo", "HTTP/1.1", requestJsonNode);
        assertHeaders(Map.of(
                "host", "localhost:8080",
                "user-agent", "qwerty",
                "content-type", "application/json",
                "content-length", "231"),
                requestJsonNode);
        assertEquals("", requestJsonNode.path("params").asText());
        assertEquals("", requestJsonNode.path("uploadedFilesToContentTypes").asText());
        assertEquals("", requestJsonNode.path("nonFormBodyInputStream").asText());
        assertEquals("231", requestJsonNode.path("nonFormBodyDataSize").asText());
    }

    @Test
    public void postRequestWithHeaderEndMissing_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_header_missing_end_request.txt");
        String exceptedResponse = getResourceFileAsString("post_header_missing_end_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void validPostMultipartRequest_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_multipart_correct_request.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        String[] headerAndBody = response.split(DOUBLE_CRLF);
        assertEquals(OK_200_JSON_RESPONSE_STATUS, headerAndBody[0]);
        JsonNode requestJsonNode = parseJson(headerAndBody[1]);
        assertStatusLine("POST", "/echo", "HTTP/1.1", requestJsonNode);
        assertHeaders(Map.of(
                "host", "localhost:8080",
                "user-agent", "qwerty",
                "content-type", "multipart/form-data; boundary=---------------------------60026920429834574901393671041",
                "content-length", "402"),
                requestJsonNode);
        assertParams(Map.of("uploadedFileName", "small_text_file.txt"), requestJsonNode);
        assertEquals("null", requestJsonNode.path("nonFormBodyInputStream").asText());
        JsonNode uploadedFilesJsonNode = requestJsonNode.path("uploadedFilesToContentTypes");
        assertTrue(uploadedFilesJsonNode.fields().next().getKey().contains("text_27_bytes.txt"));
        assertEquals("text/plain", uploadedFilesJsonNode.fields().next().getValue().asText());
    }

    @Test
    public void postMultipartRequestMissingLastBoundary_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_multipart_missing_last_boundary_request.txt");
        String exceptedResponse = getResourceFileAsString("post_multipart_missing_last_boundary_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void postMultipartRequestNoBoundaryInHeader_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_multipart_no_boundary_in_header_request.txt");
        String exceptedResponse = getResourceFileAsString("post_multipart_no_boundary_in_header_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void postMultipartRequestNoPartHeader_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_multipart_no_part_header_request.txt");
        String exceptedResponse = getResourceFileAsString("post_multipart_no_part_header_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void postMultipartRequestLessThanTwoBoundaries_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_multipart_less_than_two_boundaries_request.txt");
        String exceptedResponse = getResourceFileAsString("post_multipart_less_than_two_boundaries_response.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        assertEquals(exceptedResponse, response);
    }

    @Test
    public void validPostUrlEncodedRequest_sendToServer_getCorrectResponse() {
        // given
        String request = getResourceFileAsString("post_urlencoded_correct_request.txt");

        // when
        String response = sendRequestGetBackResponseAsString(request);

        // then
        String[] headerAndBody = response.split(DOUBLE_CRLF);
        assertEquals(OK_200_JSON_RESPONSE_STATUS, headerAndBody[0]);
        JsonNode requestJsonNode = parseJson(headerAndBody[1]);
        assertStatusLine("POST", "/echo", "HTTP/1.1", requestJsonNode);
        assertHeaders(Map.of(
                "host", "localhost:8080",
                "user-agent", "qwerty",
                "content-type", "application/x-www-form-urlencoded",
                "content-length", "23"),
                requestJsonNode);
        assertParams(Map.of("name", "Bloomfield", "age", "99"), requestJsonNode);
        assertEquals("null", requestJsonNode.path("nonFormBodyInputStream").asText());
        assertEquals("", requestJsonNode.path("uploadedFilesToContentTypes").asText());
    }

    private static JsonNode parseJson(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, JsonNode.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void assertStatusLine(String method, String url, String protocolVersion, JsonNode requestJsonNode) {
        assertEquals(method, requestJsonNode.path("httpMethod").asText());
        assertEquals(url, requestJsonNode.path("url").asText());
        assertEquals(protocolVersion, requestJsonNode.path("protocolVersion").asText());
    }

    private static void assertHeaders(Map<String, String> expectedHeaders, JsonNode requestJsonNode) {
        assertKeyValuePairs("headers", expectedHeaders, requestJsonNode);
    }

    private static void assertParams(Map<String, String> expectedParams, JsonNode requestJsonNode) {
        assertKeyValuePairs("params", expectedParams, requestJsonNode);
    }

    private static void assertKeyValuePairs(String keyValueField, Map<String, String> expectedKeyValuePairs, JsonNode requestJsonNode) {
        JsonNode actualKeyValuePairs = requestJsonNode.path(keyValueField);
        assertEquals(expectedKeyValuePairs.size(), actualKeyValuePairs.size());
        expectedKeyValuePairs.forEach((key, value) -> assertEquals(value, actualKeyValuePairs.path(key).asText()));
    }

    private InputStream sendRequestGetBackResponseInputStream(String request) {
        byte[] requestBytes = request.getBytes(StandardCharsets.US_ASCII);
        InputStream responseInputStream = null;
        try {
            Socket socket = new Socket(InetAddress.getLoopbackAddress(), SERVER_PORT);
            socket.getOutputStream().write(requestBytes);
            responseInputStream = socket.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseInputStream;
    }

    private String sendRequestGetBackResponseAsString(String request) {
        InputStream responseInputStream = sendRequestGetBackResponseInputStream(request);
        String response = null;
        try {
            response = new String(responseInputStream.readAllBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
