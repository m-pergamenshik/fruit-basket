package com.pergam.courses.httpserver.request;

import com.pergam.courses.httpserver.HttpServerConfig;
import com.pergam.courses.httpserver.common.ContentType;
import com.pergam.courses.httpserver.request.body.HttpRequestBody;
import com.pergam.courses.httpserver.request.body.HttpRequestBodySaver;
import com.pergam.courses.httpserver.utils.HeaderUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.pergam.courses.httpserver.common.HttpHeader.CONTENT_LENGTH;
import static com.pergam.courses.httpserver.common.HttpHeader.CONTENT_TYPE;
import static com.pergam.courses.httpserver.common.MediaType.APPLICATION_FORM_URL_ENCODED;
import static com.pergam.courses.httpserver.common.MediaType.MULTIPART_FORM_DATA;
import static com.pergam.courses.httpserver.request.body.HttpRequestBody.forNonFormBodyType;
import static com.pergam.courses.httpserver.request.body.multipart.MultipartBodyParser.parseMultipartBody;
import static com.pergam.courses.httpserver.request.body.urlencoded.UrlEncodedBodyParser.parseUrlEncodedBody;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.arraycopy;

public class HttpRequestParser {

    private final int requestFirstReadBufferLength;
    private final int maxAllowedRequestBodyLength;
    private final HttpRequestBodySaver httpRequestBodySaver;

    public HttpRequestParser(HttpServerConfig httpServerConfig) {
        this.requestFirstReadBufferLength = httpServerConfig.getRequestFirstReadBufferLength();
        this.maxAllowedRequestBodyLength = httpServerConfig.getMaxAllowedRequestBodyLength();
        this.httpRequestBodySaver = new HttpRequestBodySaver(httpServerConfig);
    }

    public HttpRequest parseRequest(InputStream inputStream) throws IOException {
        byte[] bytesFromFirstReadBuffer = new byte[this.requestFirstReadBufferLength]; // for safety we assume request head must fit into 1st read
        int actualBytesReadFromFirstRead = inputStream.read(bytesFromFirstReadBuffer); // BufferedInputStream is unnecessary
        final int headerEndBytePosition = HeaderUtils.findHeaderLastBytePosition(bytesFromFirstReadBuffer, actualBytesReadFromFirstRead);
        if (headerEndBytePosition == -1) {
            throw new BadRequestException(format("End of request head not found in first %d bytes", this.requestFirstReadBufferLength));
        }
        HttpRequestHead head = parseHead(bytesFromFirstReadBuffer, headerEndBytePosition);
        HttpRequestBody body = null;

        if (isRequestBodyExpected(head)) {
            int bodyLength = validateAndReturnBodyLength(head);
            String contentTypeHeaderRawValue = head.getHeaders().get(CONTENT_TYPE.toString());
            ContentType contentType = new ContentType(contentTypeHeaderRawValue);
            InputStream savedBodyInputStream = this.httpRequestBodySaver.saveBodyAndReturnAsStream(
                    inputStream, bodyLength, bytesFromFirstReadBuffer,
                    actualBytesReadFromFirstRead, headerEndBytePosition);
            body = parseBody(savedBodyInputStream, contentType);
        }

        return new HttpRequest(head, body);
    }

    private static HttpRequestHead parseHead(byte[] bytes, int headerEndBytePosition) {
        int byteLengthWithoutDoubleCRLF = headerEndBytePosition - 3;
        byte[] headBytes = new byte[byteLengthWithoutDoubleCRLF];
        arraycopy(bytes, 0, headBytes, 0, byteLengthWithoutDoubleCRLF);
        String headAsString = new String(headBytes, StandardCharsets.US_ASCII);
        return HttpRequestHeadParser.parse(headAsString);
    }

    private static boolean isRequestBodyExpected(HttpRequestHead head) {
        return head.getStartLine().getHttpMethod() == HttpMethod.POST
                && head.getHeaders().containsKey(CONTENT_LENGTH.toString()) // we don't support chunked encoding (https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Transfer-Encoding)
                && head.getHeaders().containsKey(CONTENT_TYPE.toString());
    }

    private int validateAndReturnBodyLength(HttpRequestHead head) {
        int declaredBodyLength = parseInt(head.getHeaders().get(CONTENT_LENGTH.toString()));
        if (declaredBodyLength < 1 || declaredBodyLength > maxAllowedRequestBodyLength) {
            throw new BadRequestException("Request body size must be between 1 and " + maxAllowedRequestBodyLength);
        }
        return declaredBodyLength;
    }

    private static HttpRequestBody parseBody(InputStream savedBodyInputStream, ContentType contentType) throws IOException {
        String mediaType = contentType.getMediaType();
        if (MULTIPART_FORM_DATA.toString().equals(mediaType)) {
            return parseMultipartBody(savedBodyInputStream, contentType);
        } else if (APPLICATION_FORM_URL_ENCODED.toString().equals(mediaType)) {
            return parseUrlEncodedBody(savedBodyInputStream);
        } else {
            return forNonFormBodyType(savedBodyInputStream);
        }
    }
}
