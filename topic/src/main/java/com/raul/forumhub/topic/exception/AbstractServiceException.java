package com.raul.forumhub.topic.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbstractServiceException extends RuntimeException {

    private String detailMessage;

    public AbstractServiceException(String detailMessage){
        super(detailMessage);
    }

}
