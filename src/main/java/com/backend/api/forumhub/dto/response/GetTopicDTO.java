package com.backend.api.forumhub.dto.response;

import com.backend.api.forumhub.domain.*;

import java.time.LocalDateTime;
import java.util.Set;

public record GetTopicDTO(Long id, String title, String message, LocalDateTime createdAt, Status status, User author, Course course, Set<Answer> answer) {

    public GetTopicDTO(Topic topic){
        this(topic.getId(), topic.getTitle(), topic.getMessage(), topic.getCreatedAt(), topic.getStatus(), topic.getAuthor(), topic.getCourse(), topic.getAnswers());
    }
}
