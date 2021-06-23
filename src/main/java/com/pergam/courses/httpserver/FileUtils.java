package com.pergam.courses.httpserver;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class FileUtils {

    public static String getResourceFileAsString(String fileName) {
        try {
            File file = new File(FileUtils.class.getClassLoader().getResource(fileName).toURI());
            return Files.readString(file.toPath());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] getResourceFileAsBytes(String fileName) {
        try {
            File file = new File(FileUtils.class.getClassLoader().getResource(fileName).toURI());
            return Files.readAllBytes(file.toPath());
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getResourceFileAsInputStream(String fileName) {
        try {
            File file = new File(FileUtils.class.getClassLoader().getResource(fileName).toURI());
            return new BufferedInputStream(new FileInputStream(file));
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InputStream getLocalFileAsInputStream(File file) {
        try {
            return new BufferedInputStream(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
