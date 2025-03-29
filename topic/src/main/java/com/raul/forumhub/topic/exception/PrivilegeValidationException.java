package com.raul.forumhub.topic.exception;

public class PrivilegeValidationException extends RuntimeException{

    public PrivilegeValidationException(String detailMessage) {
        super(detailMessage);
    }

}
