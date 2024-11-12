package com.raul.forumhub.topic.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientException;

@Getter
public class RestClientException extends WebClientException {

    private final HttpStatusCode httpStatusCode;

    public RestClientException(HttpStatusCode httpStatusCode, String detailMessage){
        super(detailMessage);
        this.httpStatusCode = httpStatusCode;
    }
}
