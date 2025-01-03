package com.raul.forumhub.user.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstanceNotFoundException extends RuntimeException {

    public InstanceNotFoundException(String msg) {
        super(msg);
    }
}
