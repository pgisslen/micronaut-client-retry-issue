package com.example;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Recoverable;
import io.micronaut.retry.annotation.Retryable;
import io.reactivex.Single;
import javax.validation.constraints.NotNull;

@Client("http://localhost:9999")
@Recoverable
@Retryable(delay = "10ms", attempts = "2", maxDelay = "1s")
public interface FooClient {

    @Post("/foo")
    Single<String> post(@Body @NotNull FooDTO foo);
}
