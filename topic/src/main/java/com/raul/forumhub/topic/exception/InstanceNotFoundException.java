package com.raul.forumhub.topic.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceNotFoundException extends AbstractServiceException {

    public InstanceNotFoundException(String detailMessage) {
        super(detailMessage);
    }
}
