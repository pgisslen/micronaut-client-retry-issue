package com.example;

import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.EmbeddedApplication;
import io.micronaut.test.annotation.MicronautTest;
import javax.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.mock.action.ExpectationResponseCallback;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.verify.VerificationTimes;
import static org.mockserver.model.HttpClassCallback.callback;
import static org.mockserver.model.HttpRequest.request;

@MicronautTest
@TestInstance(Lifecycle.PER_CLASS)
public class DemoTest {

    @Inject
    private EmbeddedApplication application;
    @Inject
    private RetryableClient retryableClient;
    @Inject
    private OneTryClient oneTryClient;
    @Inject
    OneTryClientFallback fallback;
    private HttpRequest endpoint;
    private ClientAndServer server9999;

    @BeforeAll
    public void beforeAll() {
        Assertions.assertTrue(application.isRunning());
        endpoint = request().withPath("/foo").withMethod("POST");
        server9999 = ClientAndServer.startClientAndServer(9999);
        ClientAndServer.startClientAndServer(9998)
            .when(endpoint)
            .respond(HttpResponse.response().withStatusCode(500));
        server9999
            .when(endpoint)
            .respond(callback().withCallbackClass(Callback.class.getName()));
    }

    @Test
    void testRetry() {
        try {
            final FooDTO foo = new FooDTO();
            foo.foo = "hello";
            retryableClient.post(foo).blockingGet();
            Assertions.fail("Expected HttpClientResponseException");
        } catch (HttpClientResponseException e) {
            Assertions.assertEquals(500, e.getStatus().getCode());
            server9999.verify(endpoint, VerificationTimes.atLeast(2));
        }
    }

    @Test
    void testFallback() {
        final FooDTO foo = new FooDTO();
        foo.foo = "hello";
        final String response = oneTryClient.post(foo).blockingGet();
        Assertions.assertEquals("failover", response);
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
