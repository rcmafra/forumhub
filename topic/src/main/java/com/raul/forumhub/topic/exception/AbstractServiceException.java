package com.raul.forumhub.topic.exception;

public class AbstractServiceException extends RuntimeException {

    public AbstractServiceException(String detailMessage){
        super(detailMessage);
    }

}
