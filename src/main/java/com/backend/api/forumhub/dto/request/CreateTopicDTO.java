package com.backend.api.forumhub.dto.request;

public record CreateTopicDTO(String title, String message, Long user_id, Long course_id) {
}
