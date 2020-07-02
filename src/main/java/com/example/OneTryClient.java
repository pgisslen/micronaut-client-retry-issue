package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Recoverable;
import io.reactivex.Single;
import javax.validation.constraints.NotNull;

@Recoverable
@Client("http://localhost:9998")
public interface OneTryClient {

    @Post("/foo")
    Single<HttpResponse<?>> post(@Body @NotNull FooDTO foo);
}
