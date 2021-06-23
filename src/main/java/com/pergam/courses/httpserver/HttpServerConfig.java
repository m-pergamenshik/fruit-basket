package com.pergam.courses.httpserver;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class HttpServerConfig {

    public static HttpServerConfig defaultConfig() {
        return HttpServerConfig.builder().build();
    }

    @Builder.Default
    private final int requestFirstReadBufferLength = 8 * 1024;

    @Builder.Default
    private final int requestBodyReadBufferLength = 16 * 1024;

    @Builder.Default
    private final int maxInMemoryRequestBodyStorage = 32 * 1024;

    @Builder.Default
    private final int maxAllowedRequestBodyLength = 10 * 1024 * 1024;
}
