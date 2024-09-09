package com.raul.forumhub.topic.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class RestClientException extends RuntimeException {

    private HttpStatusCode httpStatusCode;

    public RestClientException(HttpStatusCode httpStatusCode, String detailMessage){
        super(detailMessage);
        this.httpStatusCode = httpStatusCode;
    }
}
