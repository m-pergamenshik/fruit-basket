package com.pergam.courses.httpserver.handler;

import com.pergam.courses.httpserver.request.HttpMethod;
import com.pergam.courses.httpserver.request.HttpRequest;
import com.pergam.courses.httpserver.response.HttpResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public class HttpHandlerRegistrationTest {

    @Test
    void handlersWithCompliantMethods_registerHttpEndpoints_registersCompliantMethods() {

        // given
        Object[] handlers = {new HttpHandlerWithCompliantMethods1(),
                new HttpHandlerWithCompliantMethods2()};

        Set<HttpEndpointKey> expectedEndpointKeys = Set.of(
                new HttpEndpointKey(HttpMethod.GET, "/compliantMethod3"),
                new HttpEndpointKey(HttpMethod.POST, "/compliantMethod4"),
                new HttpEndpointKey(HttpMethod.GET, "/compliantMethod5"));

        // when
        Map<HttpEndpointKey, HttpRequestHandler> registeredEndpoints = HttpHandlerRegistration.mapEndpointsToRequestHandlers(handlers);

        // then
        assertEquals(expectedEndpointKeys, registeredEndpoints.keySet());
    }

    @Test
    void handlersWithNonCompliantMethods_registerHttpEndpoints_throwsExceptionListingNonCompliantMethods() {

        // given
        Object[] handlers = {new HttpHandlerWithCompliantMethods1(),
                new HttpHandlerWithNonCompliantMethods()};

        // when
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> HttpHandlerRegistration.mapEndpointsToRequestHandlers(handlers));

        // then
        String exceptionMessage = exception.getMessage();
        assertTrue(exceptionMessage.contains("methodWithWrongReturnType"));
        assertTrue(exceptionMessage.contains("methodWithWrongParam"));
        assertTrue(exceptionMessage.contains("methodWithNoParams"));
        assertTrue(exceptionMessage.contains("methodWithWrongAccessModifier"));
        assertTrue(exceptionMessage.contains("methodWithMoreThanOneParam"));
        assertFalse(exceptionMessage.contains("compliantMethod2"));
        assertFalse(exceptionMessage.contains("compliantMethod3"));
    }

    @SuppressWarnings("SameReturnValue")
    public static class HttpHandlerWithNonCompliantMethods {

        @HttpEndpoint(method = HttpMethod.GET, url = "/methodWithWrongReturnType")
        public HttpRequest methodWithWrongReturnType(HttpRequest request) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.POST, url = "/methodWithWrongParam")
        public HttpResponse methodWithWrongParam(String request) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.GET, url = "/compliantMethod1")
        public HttpResponse compliantMethod1(HttpRequest request) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.POST, url = "/methodWithNoParams")
        public HttpResponse methodWithNoParams() {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.POST, url = "/methodWithWrongAccessModifier")
        private HttpResponse methodWithWrongAccessModifier(HttpRequest request) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.GET, url = "/methodWithMoreThanOneParam")
        public HttpResponse methodWithMoreThanOneParam(HttpRequest request, String string) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.POST, url = "/compliantMethod2")
        public HttpResponse compliantMethod2(HttpRequest request) {
            return null;
        }
    }

    @SuppressWarnings("SameReturnValue")
    public static class HttpHandlerWithCompliantMethods1 {

        public HttpResponse nonAnnotatedMethod(HttpRequest request) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.GET, url = "/compliantMethod3")
        public HttpResponse compliantMethod3(HttpRequest request) {
            return null;
        }

    }

    @SuppressWarnings("SameReturnValue")
    public static class HttpHandlerWithCompliantMethods2 {

        @HttpEndpoint(method = HttpMethod.POST, url = "/compliantMethod4")
        public HttpResponse compliantMethod4(HttpRequest request) {
            return null;
        }

        public HttpResponse nonAnnotatedMethod(HttpRequest request) {
            return null;
        }

        @HttpEndpoint(method = HttpMethod.GET, url = "/compliantMethod5")
        public HttpResponse compliantMethod5(HttpRequest request) {
            return null;
        }
    }
}
