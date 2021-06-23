package com.pergam.courses.httpserver.request.body.multipart;

import com.pergam.courses.httpserver.request.BadRequestException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static java.lang.System.arraycopy;

public class MultipartBoundaryLineSearch {

    private static final String TWO_HYPHENS = "--";

    /**
     * Calculates positions of the first byte of each boundary line in a multipart body.
     * A boundary line is the boundary prefixed by two hyphens.
     * Two hyphens before each boundary (and two hyphens after last boundary) is required:
     * https://www.w3.org/Protocols/rfc1341/7_2_Multipart.html
     */
    public static int[] getBoundaryLinePositions(ByteBuffer bodyByteBuffer, String boundary) {
        byte[] boundaryPrefixedWithTwoHyphensBytes = (TWO_HYPHENS + boundary).getBytes(StandardCharsets.US_ASCII); // boundaries must be US-ASCII encoded (https://stackoverflow.com/a/30194096)
        int[] boundaryLinePositions = new int[0];

        if (bodyByteBuffer.remaining() > boundaryPrefixedWithTwoHyphensBytes.length * 2) { // rough check whether boundary search makes sense
            int searchWindowPosition = 0;
            byte[] searchWindow = new byte[4 * 1024 + boundaryPrefixedWithTwoHyphensBytes.length];
            int firstReadLength = Math.min(bodyByteBuffer.remaining(), searchWindow.length);
            bodyByteBuffer.get(searchWindow, 0, firstReadLength);
            int newBytesLength = firstReadLength - boundaryPrefixedWithTwoHyphensBytes.length;

            do {
                for (int j = 0; j < newBytesLength; j++) {
                    for (int i = 0; i < boundaryPrefixedWithTwoHyphensBytes.length; i++) {
                        if (searchWindow[j + i] != boundaryPrefixedWithTwoHyphensBytes[i]) {
                            break;
                        }
                        if (i == boundaryPrefixedWithTwoHyphensBytes.length - 1) {
                            int[] newBoundaryPositions = new int[boundaryLinePositions.length + 1];
                            arraycopy(boundaryLinePositions, 0, newBoundaryPositions, 0, boundaryLinePositions.length);
                            newBoundaryPositions[boundaryLinePositions.length] = searchWindowPosition + j;
                            boundaryLinePositions = newBoundaryPositions;
                        }
                    }
                }
                searchWindowPosition += newBytesLength;

                arraycopy(searchWindow, searchWindow.length - boundaryPrefixedWithTwoHyphensBytes.length, searchWindow, 0, boundaryPrefixedWithTwoHyphensBytes.length);

                newBytesLength = searchWindow.length - boundaryPrefixedWithTwoHyphensBytes.length;
                newBytesLength = Math.min(bodyByteBuffer.remaining(), newBytesLength);
                bodyByteBuffer.get(searchWindow, boundaryPrefixedWithTwoHyphensBytes.length, newBytesLength);
            } while (newBytesLength > 0);
        }

        int boundaryLinesTotal = boundaryLinePositions.length;

        if (boundaryLinesTotal < 2) {
            throw new BadRequestException("Multipart body contains less than two boundaries");
        }

        int lastBoundaryLinePosition = boundaryLinePositions[boundaryLinesTotal - 1];
        verifyLastBoundaryPostFixedWithTwoHyphens(bodyByteBuffer, lastBoundaryLinePosition, boundaryPrefixedWithTwoHyphensBytes);

        return boundaryLinePositions;
    }

    private static void verifyLastBoundaryPostFixedWithTwoHyphens(ByteBuffer bodyByteBuffer,
                                                                  int lastBoundaryLinePosition,
                                                                  byte[] boundaryPrefixedWithTwoHyphensBytes) {
        bodyByteBuffer.position(lastBoundaryLinePosition + boundaryPrefixedWithTwoHyphensBytes.length);
        byte[] twoHyphensBytes = TWO_HYPHENS.getBytes();
        byte[] lastBytesOfBoundaryLine = new byte[twoHyphensBytes.length];
        bodyByteBuffer.get(lastBytesOfBoundaryLine);
        if (!Arrays.equals(twoHyphensBytes, lastBytesOfBoundaryLine)) {
            throw new BadRequestException("Multipart body is missing closing boundary (post-fixed with two hyphens)");
        }
    }
}
