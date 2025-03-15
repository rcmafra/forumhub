package com.raul.forumhub.topic.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String detailMessage){
        super(detailMessage);
    }

}
