package com.backend.api.forumhub.dto.request;

public record CreateTopicDTO(String title, String message, Long course_id) {
}
