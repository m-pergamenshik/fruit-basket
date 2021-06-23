package com.pergam.courses.mainapp;

import com.pergam.courses.httpserver.HttpServerConfig;
import com.pergam.courses.httpserver.handler.sample.*;
import com.pergam.courses.httpserver.HttpServer;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        Object[] requestHandlers = {
                new EchoRequestHandler(),
                new MultipartFormHandler(),
                new UrlEncodedFormHandler(),
                new SlowRequestHandler(),
                new ResourceFileServingHandler("happy_dog_image_13449_bytes.jpg", "image/jpeg"),
                new LocalFileServingHandler(new File("C:\\path\\to\\video.mp4"), "video/mp4")};

        HttpServer httpServer = new HttpServer(8080, HttpServerConfig.defaultConfig(), requestHandlers);
        httpServer.start();
        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        httpServer.shutDown();
    }

}
