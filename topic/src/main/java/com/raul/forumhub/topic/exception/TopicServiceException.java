package com.raul.forumhub.topic.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopicServiceException extends AbstractServiceException {

    public TopicServiceException(String detailMessage){
        super(detailMessage);
    }

}
