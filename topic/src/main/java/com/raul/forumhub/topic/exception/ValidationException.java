package com.raul.forumhub.topic.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String detailMessage){
        super(detailMessage);
    }
}
