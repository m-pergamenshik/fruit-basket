package com.pergam.courses.httpserver.request.body;

import com.pergam.courses.httpserver.HttpServerConfig;

import java.io.*;

import static java.lang.System.arraycopy;
import static java.nio.file.Files.createTempFile;

public class HttpRequestBodySaver {

    private final int firstReadBufferLength;
    private final int bodyReadBufferLength;
    private final int maxInMemoryBodyStorage;

    public HttpRequestBodySaver(HttpServerConfig httpServerConfig) {
        this.firstReadBufferLength = httpServerConfig.getRequestFirstReadBufferLength();
        this.bodyReadBufferLength = httpServerConfig.getRequestBodyReadBufferLength();
        this.maxInMemoryBodyStorage = httpServerConfig.getMaxInMemoryRequestBodyStorage();
    }

    public InputStream saveBodyAndReturnAsStream(InputStream inputStream,
                                                  int bodyLength,
                                                  byte[] bytesFromFirstRead,
                                                  int bytesFromFirstReadLength,
                                                  int headerEndBytePosition) throws IOException {

        int bodyBytesFromFirstReadLength = bytesFromFirstReadLength - (headerEndBytePosition + 1);
        byte[] bodyBytesFromFirstRead;
        if (bodyBytesFromFirstReadLength > 0) {
            bodyBytesFromFirstRead = new byte[bodyBytesFromFirstReadLength];
            arraycopy(bytesFromFirstRead, headerEndBytePosition + 1, bodyBytesFromFirstRead, 0, bodyBytesFromFirstReadLength);
        } else {
            bodyBytesFromFirstRead = new byte[0];
        }
        if (bytesFromFirstReadLength < this.firstReadBufferLength) {
            return new ByteArrayInputStream(bodyBytesFromFirstRead);
        } else {
            OutputStream bodyBytesOutputStream = null; // not in try-with-resources because it's not effectively final
            File tmpFile = null;
            try {
                final boolean bodyCanBeStoredInMemory = bodyLength <= this.maxInMemoryBodyStorage;
                if (bodyCanBeStoredInMemory) {
                    bodyBytesOutputStream = new ByteArrayOutputStream(bodyLength);
                } else {
                    tmpFile = createTempFile("httpUploaded_", ".tmp").toFile();
                    tmpFile.deleteOnExit();
                    bodyBytesOutputStream = new FileOutputStream(tmpFile); // BufferedOutputStream is unnecessary
                }
                if (bodyBytesFromFirstRead.length > 0) {
                    bodyBytesOutputStream.write(bodyBytesFromFirstRead);
                }
                int bodyBytesReadTotal = bodyBytesFromFirstReadLength;
                int bodyBytesReadLastIteration;

                do {
                    byte[] bodyBytesReadBuffer = new byte[this.bodyReadBufferLength];
                    bodyBytesReadLastIteration = inputStream.read(bodyBytesReadBuffer); // BufferedInputStream is unnecessary
                    bodyBytesReadTotal += bodyBytesReadLastIteration;
                    bodyBytesOutputStream.write(bodyBytesReadBuffer, 0, bodyBytesReadLastIteration); // in case bodyBytesReadTotal < BODY_READ_BUFFER_LENGTH after last byte is read
                } while (bodyBytesReadTotal < bodyLength && bodyBytesReadLastIteration == this.bodyReadBufferLength);

                if (bodyCanBeStoredInMemory) {
                    ByteArrayOutputStream baos = (ByteArrayOutputStream) bodyBytesOutputStream;
                    return new ByteArrayInputStream(baos.toByteArray());
                } else {
                    return new FileInputStream(tmpFile);
                }
            } catch (Exception e) {
                if (tmpFile != null && tmpFile.exists()) {
                    tmpFile.delete();
                }
                throw new RuntimeException(e);
            } finally {
                if (bodyBytesOutputStream != null) {
                    bodyBytesOutputStream.close();
                }
            }
        }
    }
}
