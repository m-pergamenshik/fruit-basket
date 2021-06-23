package com.pergam.courses.httpserver.request.body.multipart;

import com.pergam.courses.httpserver.common.ContentType;
import com.pergam.courses.httpserver.request.BadRequestException;
import com.pergam.courses.httpserver.request.body.HttpRequestBody;
import com.pergam.courses.httpserver.utils.HeaderUtils;
import com.pergam.courses.httpserver.utils.HttpCommonRegex;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

import static com.pergam.courses.httpserver.request.body.multipart.MultipartBoundaryLineSearch.getBoundaryLinePositions;

/**
 * we do NOT support multiple files sent via one form entry ("multipart/mixed"):
 * https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.2
 */
public class MultipartBodyParser {

    public static final int MAX_MULTIPART_HEADER_SIZE = 1024;

    public static HttpRequestBody parseMultipartBody(InputStream savedBodyInputStream, ContentType contentType) throws IOException {
        ByteBuffer bodyByteBuffer = wrapInByteBuffer(savedBodyInputStream);
        Path tempFilesDir = null;
        Map<Path, String> uploadedFilesToContentTypes = null;
        Map<String, String> params = new HashMap<>();

        String boundary = contentType.getOptionalParam().getValue();
        int[] boundaryLinePositions = getBoundaryLinePositions(bodyByteBuffer, boundary);

        byte[] headerBuffer = new byte[MAX_MULTIPART_HEADER_SIZE];
        for (int boundaryLineIndex = 0; boundaryLineIndex < boundaryLinePositions.length - 1; boundaryLineIndex++) {
            final int boundaryLineFirstBytePosition = boundaryLinePositions[boundaryLineIndex];
            bodyByteBuffer.position(boundaryLineFirstBytePosition);
            int bytesReadIntoHeaderBuffer = Math.min(bodyByteBuffer.remaining(), MAX_MULTIPART_HEADER_SIZE);
            bodyByteBuffer.get(headerBuffer, 0, bytesReadIntoHeaderBuffer);

            BufferedReader headerBufferInputStreamReader = new BufferedReader(new InputStreamReader( // using BufferedReader only because we need readLine()
                    new ByteArrayInputStream(headerBuffer, 0, bytesReadIntoHeaderBuffer)), bytesReadIntoHeaderBuffer);

            String partName;
            String partFileName = null;
            String partContentType = null;

            int lengthOfBoundaryLineWithCrLf = 2 + boundary.length() + 2; // "--" + boundary + "\r\n"
            headerBufferInputStreamReader.skip(lengthOfBoundaryLineWithCrLf); // skipping boundary line
            String[] headerFirstLineParts = headerBufferInputStreamReader.readLine().split(";");

            /**
             * Formally in multipart body parts the header is not required
             * (https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html)
             * but for simplicity we will expect it.
             */
            if (headerFirstLineParts.length < 2 || headerFirstLineParts.length > 3) {
                throw new BadRequestException("Multipart content-disposition header invalid or missing");
            }

            HttpCommonRegex.MULTIPART_CONTENT_DISPOSITION_FORM_DATA_REGEX.apply(headerFirstLineParts[0].strip(), () -> {
                throw new BadRequestException("Multipart header is missing content-disposition");
            });

            String[] partNameRegexGroups = HttpCommonRegex.MULTIPART_NAME_REGEX.apply(headerFirstLineParts[1].strip(), () -> {
                throw new BadRequestException("Multipart header is missing name");
            });
            partName = partNameRegexGroups[1];

            if (headerFirstLineParts.length == 3) {
                String[] partFileNameRegexGroups = HttpCommonRegex.MULTIPART_FILE_NAME_REGEX.apply(headerFirstLineParts[2].strip(), () -> {
                    throw new BadRequestException("Multipart header ending not recognized: " + headerFirstLineParts[2].strip());
                });
                partFileName = partFileNameRegexGroups[1];
            }

            String headerSecondLine = headerBufferInputStreamReader.readLine();
            if (!headerSecondLine.isBlank()) {
                String[] partContentTypeRegexGroups = HttpCommonRegex.MULTIPART_CONTENT_TYPE_REGEX.apply(headerSecondLine, () -> {
                    throw new BadRequestException("Unsupported multipart header: " + headerSecondLine);
                });
                partContentType = partContentTypeRegexGroups[1];
            }

            int headerLastBytePosition = HeaderUtils.findHeaderLastBytePosition(headerBuffer, bytesReadIntoHeaderBuffer);
            if (headerLastBytePosition == -1) {
                throw new BadRequestException("Multipart header exceeds MAX_MULTIPART_HEADER_SIZE of " + MAX_MULTIPART_HEADER_SIZE);
            }

            int headerLength = headerLastBytePosition + 1;
            int partDataStart = boundaryLineFirstBytePosition + headerLength;
            int partDataEnd = boundaryLinePositions[boundaryLineIndex + 1] - 2; // 2 = "\r\n"
            int partDataLength = partDataEnd - partDataStart;

            bodyByteBuffer.position(partDataStart);

            if (partContentType == null) {
                byte[] partData = new byte[partDataLength];
                bodyByteBuffer.get(partData);
                params.put(partName, new String(partData));
            } else {
                if (partDataLength > 0) {
                    if (tempFilesDir == null) {
                        tempFilesDir = Files.createTempDirectory("http_multipart_files_");
                    }
                    if (uploadedFilesToContentTypes == null) {
                        uploadedFilesToContentTypes = new HashMap<>();
                    }
                    Path uploadedFilePath = saveMultipartDataAsTempFile(bodyByteBuffer, partDataStart, partDataEnd, partFileName, tempFilesDir);
                    uploadedFilesToContentTypes.put(uploadedFilePath, partContentType);
                }
            }
        }
        return HttpRequestBody.forMultipartType(params, uploadedFilesToContentTypes);
    }

    private static ByteBuffer wrapInByteBuffer(InputStream savedBodyInputStream) throws IOException {
        if (savedBodyInputStream.getClass() == FileInputStream.class) { // ok to use instead of 'instanceof'
            FileChannel requestBodyFileChannel = ((FileInputStream) savedBodyInputStream).getChannel();
            return requestBodyFileChannel.map(FileChannel.MapMode.READ_ONLY, 0, requestBodyFileChannel.size());
        } else if (savedBodyInputStream.getClass() == ByteArrayInputStream.class) {
            byte[] bodyBytes = ((ByteArrayInputStream) savedBodyInputStream).readAllBytes();
            return ByteBuffer.wrap(bodyBytes);
        } else {
            throw new IllegalStateException("savedBodyInputStream has unexpected class " + savedBodyInputStream.getClass());
        }
    }

    private static Path saveMultipartDataAsTempFile(ByteBuffer bodyByteBuffer,
                                                    int partDataStart,
                                                    int partDataEnd,
                                                    String fileName,
                                                    Path tempFilesDir) throws IOException {
        Path uploadedFilePath = Path.of(tempFilesDir.toString(), fileName);
        FileChannel uploadedFileChannel = FileChannel.open(uploadedFilePath, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        ByteBuffer bodyBufferPartDataSlice = bodyByteBuffer.duplicate();
        bodyBufferPartDataSlice.position(partDataStart).limit(partDataEnd);
        uploadedFileChannel.write(bodyBufferPartDataSlice);
        return uploadedFilePath;
    }

}
