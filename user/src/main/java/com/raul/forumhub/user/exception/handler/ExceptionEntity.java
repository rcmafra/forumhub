package com.raul.forumhub.user.exception.handler;

import java.time.LocalDateTime;

public record ExceptionEntity(LocalDateTime timestamp, int status, String title, String detail, String instance) {
}
