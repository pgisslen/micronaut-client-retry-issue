package com.example;

import io.micronaut.http.HttpResponse;
import io.micronaut.retry.annotation.Fallback;
import io.reactivex.Single;
import javax.validation.constraints.NotNull;

@Fallback
public class OneTryClientFallback implements OneTryClient {

    @Override
    public Single<HttpResponse<?>> post(@NotNull final FooDTO foo) {
        return Single.just(HttpResponse.ok());
    }
}
