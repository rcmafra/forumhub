package com.raul.forumhub.user.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MalFormatedParamUserException extends RuntimeException {

    public MalFormatedParamUserException(String message){
        super(message);
    }
}
