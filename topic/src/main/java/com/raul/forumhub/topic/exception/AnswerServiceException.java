package com.raul.forumhub.topic.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerServiceException extends AbstractServiceException {

    public AnswerServiceException(String detailMessage) {
        super(detailMessage);
    }
}
