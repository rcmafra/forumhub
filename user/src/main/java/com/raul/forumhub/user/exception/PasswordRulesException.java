package com.raul.forumhub.user.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordRulesException extends RuntimeException {

    public PasswordRulesException(String message) {
        super(message);
    }
}
