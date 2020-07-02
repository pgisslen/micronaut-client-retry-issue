package com.example;

import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpRequest.request;

@MicronautTest
public class DemoTest {

    @Inject
    private EmbeddedApplication application;
    @Inject
    private FooClient fooClient;

    @Test
    void testItWorks() {
        Assertions.assertTrue(application.isRunning());
        final HttpRequest endpoint = request().withPath("/foo").withMethod("POST");
        final ClientAndServer server = ClientAndServer.startClientAndServer(9999);
        server
            .when(endpoint)
            .respond(callback().withCallbackClass(Callback.class.getName()));

        try {
            final FooDTO foo = new FooDTO();
            foo.foo = "hello";
            fooClient.post(foo).blockingGet();
            Assertions.fail("Expected HttpClientResponseException");
        } catch (HttpClientResponseException e) {
            Assertions.assertEquals(500, e.getStatus().getCode());
            server.verify(endpoint, VerificationTimes.atLeast(2));
        }
    }

    public static class Callback implements ExpectationResponseCallback {

        @Override
        public HttpResponse handle(HttpRequest httpRequest) {
            final String bodyAsString = httpRequest.getBodyAsString();
            if (bodyAsString == null || !bodyAsString.contains("hello")) {
                return HttpResponse.response().withStatusCode(400);
            }
            return HttpResponse.response().withStatusCode(500);
        }
    }

}
