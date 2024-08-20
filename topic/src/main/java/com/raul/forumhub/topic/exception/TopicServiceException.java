package com.raul.forumhub.topic.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicServiceException extends RuntimeException {

    private String msg;

    public TopicServiceException(String msg){
        super(msg);
    }

}
