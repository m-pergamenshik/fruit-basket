package com.pergam.courses.httpserver.handler;

import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.stream.Collectors.*;

public class HttpHandlerRegistration {

    private static final Predicate<Method> IS_METHOD_COMPATIBLE_WITH_HTTP_ENDPOINT_ANNOTATION = method -> {
        int modifiers = method.getModifiers();
        boolean hasPublicModifier = Modifier.isPublic(modifiers);
        boolean hasStaticModifier = Modifier.isStatic(modifiers);
        Class<?> returnType = method.getReturnType();
        Parameter[] params = method.getParameters();

        return hasPublicModifier
                && !hasStaticModifier
                && returnType == HttpResponse.class
                && params.length == 1
                && params[0].getType() == HttpRequest.class;
    };

    /**
     * For simplicity we do have any logic to prevent duplicate endpoint keys in httpRequestHandlers
     */
    public static Map<HttpEndpointKey, HttpRequestHandler> mapEndpointsToRequestHandlers(Object... httpRequestHandlers) {
        Map<HttpEndpointKey, HttpRequestHandler> allHttpEndpointKeysToMethods = new HashMap<>();

        for (Object httpRequestHandler : httpRequestHandlers) {
            Method[] handlerMethods = httpRequestHandler.getClass().getDeclaredMethods(); // includes private methods

            Map<Boolean, List<Method>> methodsByCompatibilityWithAnnotation = Arrays.stream(handlerMethods)
                    .filter(method -> method.getDeclaredAnnotation(HttpEndpoint.class) != null)
                    .collect(partitioningBy(IS_METHOD_COMPATIBLE_WITH_HTTP_ENDPOINT_ANNOTATION));

            List<Method> incompatibleMethods = methodsByCompatibilityWithAnnotation.get(false);
            if (!incompatibleMethods.isEmpty()) {
                reportIncompatibleMethods(incompatibleMethods);
            }

            List<Method> compatibleMethods = methodsByCompatibilityWithAnnotation.get(true);
            Map<HttpEndpointKey, HttpRequestHandler> httpEndpointKeysToMethods = compatibleMethods
                    .stream()
                    .collect(toMap(
                            HttpHandlerRegistration::annotatedMethodToHttpEndpointKey,
                            method -> new HttpRequestHandler(method, httpRequestHandler)));

            allHttpEndpointKeysToMethods.putAll(httpEndpointKeysToMethods);
        }

        return allHttpEndpointKeysToMethods;
    }

    private static HttpEndpointKey annotatedMethodToHttpEndpointKey(Method method) {
        HttpMethod httpMethod = method.getDeclaredAnnotation(HttpEndpoint.class).method();
        String url = method.getDeclaredAnnotation(HttpEndpoint.class).url();
        return new HttpEndpointKey(httpMethod, url);
    }

    private static void reportIncompatibleMethods(List<Method> incompatibleMethods) {
        Set<String> incompatibleMethodsDescription = incompatibleMethods.stream().map(Method::toString).collect(toSet());
        String incompatibleMethodsDescriptionJoined = String.join("\n", incompatibleMethodsDescription);

        throw new RuntimeException(format("Following methods are incompatible with annotation @%s:\n%s",
                HttpEndpoint.class.getName(),
                incompatibleMethodsDescriptionJoined));
    }
}
